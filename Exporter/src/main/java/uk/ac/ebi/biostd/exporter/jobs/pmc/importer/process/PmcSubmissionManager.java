package uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process;

import static uk.ac.ebi.biostd.model.Submission.canonicAttachToAttribute;

import com.pri.util.AccNoUtil;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import uk.ac.ebi.biostd.in.PMDoc;
import uk.ac.ebi.biostd.in.pagetab.SectionOccurrence;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.model.SubmissionAttributeException;
import uk.ac.ebi.biostd.out.DocumentFormatter;
import uk.ac.ebi.biostd.out.json.JSONFormatter;

@AllArgsConstructor
@Slf4j
public class PmcSubmissionManager {

    private final static String DOWNLOAD_URL = "http://europepmc.org/articles/%s/bin/%s";
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private final String userPath;

    public CompletableFuture<Void> downLoadFiles(String submissionId, String rootPath, List<String> files) {
        return CompletableFuture.allOf(files.stream()
                .map(file -> CompletableFuture.runAsync(() -> downloadFile(submissionId, rootPath, file), executor))
                .toArray(CompletableFuture[]::new));
    }

    public String getJsonSubmission(SubmissionInfo submissionInfo) throws SubmissionAttributeException, IOException {
        Submission submission = submissionInfo.getSubmission();

        String accNo = getAccNo(submissionInfo);

        submission.setAccNo(accNo);
        submission.normalizeAttributes();
        submission.addAttribute(canonicAttachToAttribute, "EuropePMC");
        submission.setRootPath(AccNoUtil.getPartitionedPath(accNo));
        submission.getRootSection().setAccNo(null);
        submissionInfo.setAccNoOriginal(accNo);
        SectionOccurrence rootSectionOcurrance = submissionInfo.getRootSectionOccurance();
        rootSectionOcurrance.setOriginalAccNo(null);
        rootSectionOcurrance.setPrefix(null);
        rootSectionOcurrance.setSuffix(null);

        return getJsonBody(submissionInfo);
    }

    private String getJsonBody(SubmissionInfo submissionInfo) throws IOException {
        PMDoc pmDoc = new PMDoc();
        pmDoc.addSubmission(submissionInfo);

        StringBuilder stringBuilder = new StringBuilder();
        DocumentFormatter documentFormatter = new JSONFormatter(stringBuilder, true);
        documentFormatter.format(pmDoc);
        return stringBuilder.toString();
    }

    private String getAccNo(SubmissionInfo submissionInfo) {
        String acc = submissionInfo.getSubmission().getRootSection().getAccNo();
        if (acc == null) {
            acc = submissionInfo.getSubmission().getAccNo();
        }

        if (acc.startsWith("!")) {
            acc = acc.substring(1);
        }

        return acc;
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
