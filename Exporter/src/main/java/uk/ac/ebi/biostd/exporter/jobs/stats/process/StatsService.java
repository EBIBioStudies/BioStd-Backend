package uk.ac.ebi.biostd.exporter.jobs.stats.process;

import java.io.File;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.exporter.jobs.stats.StatsProperties;
import uk.ac.ebi.biostd.exporter.jobs.stats.model.SubStats;
import uk.ac.ebi.biostd.exporter.model.Submission;
import uk.ac.ebi.biostd.exporter.model.SubmissionStats;
import uk.ac.ebi.biostd.exporter.persistence.dao.SubmissionDao;

@AllArgsConstructor
@Service
public class StatsService {
    private final SubmissionDao submissionDao;
    private final StatsProperties startProperties;

    SubStats processSubmission(Submission submission) {
        String accNo = submission.getAccno();
        SubmissionStats submissionStats = new SubmissionStats();

        try {
            submissionStats = submissionDao.getSubmissionStats(submission.getId());
        } catch (EmptyResultDataAccessException exception) {
            submissionStats.setFilesCount(0);
            submissionStats.setFilesSize(0);
        }

        return SubStats.builder()
                .accNo(accNo)
                .imaging(submission.isImagingSubmission())
                .files(submissionStats.getFilesCount())
                .filesSize(submissionStats.getFilesSize())
                .subFileSize(getFileSize(accNo, submission.getRelPath()))
                .build();
    }

    private Long getFileSize(String accNo, String rootPath) {
        String fullPath = String.format("%s/%s/%s.pagetab.tsv", startProperties.getBasePath(), rootPath, accNo);
        return new File(fullPath).length();
    }
}
