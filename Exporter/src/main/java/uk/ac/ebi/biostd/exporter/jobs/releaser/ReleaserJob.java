package uk.ac.ebi.biostd.exporter.jobs.releaser;

import static uk.ac.ebi.biostd.exporter.utils.DateUtils.now;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.biostd.exporter.persistence.dao.SubmissionDao;
import uk.ac.ebi.biostd.exporter.persistence.model.SubAndUserInfo;

@Component
@AllArgsConstructor
public class ReleaserJob {

    private final SubmissionDao submissionDao;

    @Transactional
    public void execute() {
        List<SubAndUserInfo> toRelease = submissionDao.getPendingToReleaseSub(0L, now().toEpochSecond());
        toRelease.stream().map(SubAndUserInfo::getSubId).forEach(submissionDao::releaseSubmission);
    }
}
