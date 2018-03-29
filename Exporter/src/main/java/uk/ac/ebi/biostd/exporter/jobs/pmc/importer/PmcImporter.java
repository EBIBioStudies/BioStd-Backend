package uk.ac.ebi.biostd.exporter.jobs.pmc.importer;

import static java.util.stream.Collectors.toList;

import com.pri.util.AccNoUtil;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process.CvsTvsParser;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process.PmcSubmissionManager;
import uk.ac.ebi.biostd.in.PMDoc;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.remote.dto.SubmissionResultDto;
import uk.ac.ebi.biostd.remote.service.RemoteService;
import uk.ac.ebi.biostd.treelog.ErrorCounterImpl;
import uk.ac.ebi.biostd.treelog.LogNode.Level;
import uk.ac.ebi.biostd.treelog.SimpleLogNode;

@AllArgsConstructor
public class PmcImporter {

    private final PmcImportProperties properties;
    private final CvsTvsParser cvsTvsParser;
    private final PmcSubmissionManager submissionManager;
    private final RemoteService remoteService;

    public void execute() throws ExecutionException, InterruptedException {
        Iterator<File> fileIterator = FileUtils.iterateFiles(new File(properties.getImportPath()), null, false);
        String sessionId = remoteService.login(properties.getUser(), properties.getPassword()).getSessid();

        while (fileIterator.hasNext()) {
            SimpleLogNode topLn = new SimpleLogNode(Level.SUCCESS, "Parsing file: ", new ErrorCounterImpl());
            PMDoc doc = cvsTvsParser.parse(fileIterator.next(), '\t', topLn);

            for (SubmissionInfo subInfo : doc.getSubmissions()) {
                CompletableFuture<Void> result = downloadFiles(subInfo)
                        .thenRunAsync(() -> submitSubmission(sessionId, subInfo));
                result.get();
            }
        }
    }

    @SneakyThrows
    private SubmissionResultDto submitSubmission(String sessionId, SubmissionInfo subInfo) {
        String jsonBody = submissionManager.getJsonSubmission(subInfo);
        SubmissionResultDto result = remoteService.createJsonSubmission(sessionId, jsonBody);
        return result;
    }

    @SneakyThrows
    private CompletableFuture<Void> downloadFiles(SubmissionInfo submissionInfo) {
        String pmcId = getPMCId(submissionInfo);
        List<String> files = submissionInfo.getFileOccurrences().stream()
                .map(file -> file.getFileRef().getName())
                .collect(toList());

        return submissionManager
                .downLoadFiles(pmcId, AccNoUtil.getPartitionedPath(submissionInfo.getSubmission().getAccNo()), files);
    }

    private String getPMCId(SubmissionInfo submissionInfo) {
        String accNo = submissionInfo.getSubmission().getAccNo();
        int accNoIndex = accNo.lastIndexOf("PMC");

        return accNo.substring(accNoIndex);
    }

}
