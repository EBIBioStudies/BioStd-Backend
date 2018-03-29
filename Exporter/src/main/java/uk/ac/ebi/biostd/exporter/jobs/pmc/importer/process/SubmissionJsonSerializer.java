package uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process;


import static uk.ac.ebi.biostd.model.Submission.canonicAttachToAttribute;

import com.pri.util.AccNoUtil;
import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.in.PMDoc;
import uk.ac.ebi.biostd.in.pagetab.SectionOccurrence;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.model.SubmissionAttributeException;
import uk.ac.ebi.biostd.out.DocumentFormatter;
import uk.ac.ebi.biostd.out.json.JSONFormatter;

@Component
@Slf4j
public class SubmissionJsonSerializer {

    private static final String EUROPE_PMC_ACC_NO = "EuropePMC";

    public Optional<String> getJson(SubmissionInfo submissionInfo) {
        Submission submission = submissionInfo.getSubmission();
        Optional<String> optionalJson = Optional.empty();

        try {
            String accNo = getAccNo(submissionInfo);
            submission.setAccNo(accNo);
            submission.normalizeAttributes();
            submission.addAttribute(canonicAttachToAttribute, EUROPE_PMC_ACC_NO);
            submission.setRootPath(AccNoUtil.getPartitionedPath(accNo));
            submission.getRootSection().setAccNo(null);
            submissionInfo.setAccNoOriginal(accNo);

            SectionOccurrence rootSectionOccurrence = submissionInfo.getRootSectionOccurance();
            rootSectionOccurrence.setOriginalAccNo(null);
            rootSectionOccurrence.setPrefix(null);
            rootSectionOccurrence.setSuffix(null);

            optionalJson = Optional.of(getJsonBody(submissionInfo));
        } catch (SubmissionAttributeException | IOException e) {
            log.error("Submission with accno {} could not be processed", submissionInfo.getAccNoOriginal(), e);
        }

        return optionalJson;
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
}
