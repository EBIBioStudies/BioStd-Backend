package uk.ac.ebi.biostd.exporter.jobs.stats.process;

import java.io.File;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.exporter.jobs.stats.StatsProperties;
import uk.ac.ebi.biostd.exporter.jobs.stats.model.SubStats;
import uk.ac.ebi.biostd.exporter.model.Submission;
import uk.ac.ebi.biostd.exporter.persistence.dao.SubmissionDao;

@AllArgsConstructor
@Service
public class StatsService {
    private final SubmissionDao submissionDao;
    private final StatsProperties startProperties;

    SubStats processSubmission(Submission submission) {
        String accNo = submission.getAccno();
        Submission submissionStats = submissionDao.getSubmissionStats(accNo);

        return SubStats.builder()
                .accNo(accNo)
                .imaging(submissionStats.isImagingSubmission())
                .files((int) submissionStats.getFilesCount())
                .filesSize(submissionStats.getFilesSize())
                .subFileSize(getFileSize(accNo, submissionStats.getRelPath()))
                .build();
    }

    private Long getFileSize(String accNo, String rootPath) {
        String fullPath = String.format("%s/%s/%s.pagetab.tsv", startProperties.getBasePath(), rootPath, accNo);
        return new File(fullPath).length();
    }
}
