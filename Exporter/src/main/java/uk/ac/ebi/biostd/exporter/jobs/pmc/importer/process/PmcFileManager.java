package uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process;

import com.pri.util.AccNoUtil;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.PmcImportProperties;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;

@Slf4j
@Component
public class PmcFileManager {

    private final static String DOWNLOAD_URL = "http://europepmc.org/articles/%s/bin/%s";
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private final String userPath;

    public PmcFileManager(PmcImportProperties properties) {
        this.userPath = properties.getSubmitterUserPath();
    }

    @SneakyThrows
    public void downloadFiles(SubmissionInfo submissionInfo) {
        String pmcId = getPMCId(submissionInfo);
        String partitionPath = AccNoUtil.getPartitionedPath(submissionInfo.getSubmission().getAccNo());

        CompletableFuture.allOf(submissionInfo.getFileOccurrences().stream()
                .map(file -> file.getFileRef().getName())
                .map(file -> CompletableFuture.runAsync(() -> downloadFile(pmcId, partitionPath, file), executor))
                .toArray(CompletableFuture[]::new)).join();
    }

    private String getPMCId(SubmissionInfo submissionInfo) {
        String accNo = submissionInfo.getSubmission().getAccNo();
        int accNoIndex = accNo.lastIndexOf("PMC");

        return accNo.substring(accNoIndex);
    }

    private void downloadFile(String submissionId, String rootPath, String fileName) {
        try {
            URL website = new URL(String.format(DOWNLOAD_URL, submissionId, fileName));
            FileUtils.copyURLToFile(website, Paths.get(userPath + "/" + rootPath + "/" + fileName).toFile());
        } catch (IOException exception) {
            log.error("There were some error downloading the submission", exception);
        }
    }
}
