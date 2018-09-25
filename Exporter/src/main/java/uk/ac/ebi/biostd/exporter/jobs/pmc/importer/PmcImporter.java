package uk.ac.ebi.biostd.exporter.jobs.pmc.importer;

import static com.google.common.collect.Streams.stream;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.apache.commons.io.FileUtils.iterateFiles;
import static uk.ac.ebi.biostd.exporter.utils.FileUtil.filesAsStringList;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process.PmcFileManager;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process.SubmissionJsonSerializer;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process.SubmissionsParser;
import uk.ac.ebi.biostd.exporter.utils.FileUtil;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.remote.service.RemoteService;

@Component
@Slf4j
@AllArgsConstructor
public class PmcImporter {

    private static final String SUBMIT_MSN_FORMAT = "sub # {}, '{}' from file '{}' submitted successfully";
    private static final String START_SUBMIT_MSN_FORMAT = "submitting # {}, '{}' from file '{}'";

    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    private final PmcImportProperties prop;
    private final PmcFileManager submissionManager;
    private final SubmissionJsonSerializer jsonSerializer;
    private final RemoteService remoteService;
    private final SubmissionsParser subParser;

    private final AtomicInteger fileCounter = new AtomicInteger(0);
    private final AtomicInteger subCounter = new AtomicInteger(0);

    public void importConfiguredPath() {
        exec(() -> processAndSubmit(stream(iterateFiles(new File(prop.getImportPath()), null, true))));
    }

    public void importSingleFile(String path) {
        exec(() -> processFile(new File(path), remoteService.login(prop.getUser(), prop.getPassword()).getSessid()));
    }

    public void importFilesInPathByRegex(String rootPath, String regex) {
        exec(() -> processAndSubmit(Arrays.stream(FileUtil.listFilesMatching(rootPath, regex))));
    }

    private void exec(Runnable runnable) {
        fileCounter.set(1);
        subCounter.set(1);
        runnable.run();
    }

    private void processAndSubmit(Stream<File> fileStream) {
        String sessionId = remoteService.login(prop.getUser(), prop.getPassword()).getSessid();

        List<File> files = fileStream.collect(Collectors.toList());
        log.info("processing \n {}", filesAsStringList(files));
        files.forEach(file -> processFile(file, sessionId));
    }

    private void processFile(File file, String sessionId) {
        List<SubmissionInfo> submissions = subParser.parseSubmissionFile(file);
        log.info("processing file number {}, '{}', with {} submissions",
                fileCounter.getAndIncrement(), file.toPath(), submissions.size());

        allOf(submissions.stream()
                .map(sub -> runAsync(() -> submitSubmission(sessionId, sub, file.getName()), executor))
                .toArray(CompletableFuture[]::new)).join();
    }

    private void submitSubmission(String sessionId, SubmissionInfo subInfo, String fileName) {
        try {
            int subNumber = subCounter.getAndIncrement();
            String accNo = subInfo.getAccNoOriginal();

            log.info(START_SUBMIT_MSN_FORMAT, subNumber, accNo, fileName);
            submissionManager.downloadFiles(subInfo);
            jsonSerializer.getJson(subInfo).ifPresent(json -> remoteService.createJsonSubmission(sessionId, json));
            log.info(SUBMIT_MSN_FORMAT, subNumber, accNo, fileName);
        } catch (Exception exception) {
            log.info("failing processing submission {}", subInfo.getAccNoOriginal());
            log.error("error when download files/submitting", exception);
        }
    }
}
