package uk.ac.ebi.biostd.exporter.persistence.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.biostd.exporter.model.Submission;
import uk.ac.ebi.biostd.exporter.persistence.model.SubAndUserInfo;
import uk.ac.ebi.biostd.exporter.utils.DateUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@DirtiesContext
public class SubmissionDaoTest extends PersistenceTest {

    private static final String SUB_ID = "Test-Submission-01/01/2018";
    private static final String ADMIN_EMAIL = "biostudies-dev@ebi.ac.uk";
    private static final String ADMIN_NAME = "Biostudy manager";
    private static final String PUBLIC_TAG = "Public";

    @Autowired
    private SubmissionDao submissionDao;

    @Test
    public void getPendingToReleaseSub() {
        List<SubAndUserInfo> infoResult = submissionDao.getPendingToReleaseSub(0, DateUtils.now().toEpochSecond());
        assertThat(infoResult).hasSize(1);
        assertResultInfo(infoResult.get(0));
    }

    private void assertResultInfo(SubAndUserInfo subAndUserInfo) {
        assertThat(subAndUserInfo.getAuthorEmail()).isEqualTo(ADMIN_EMAIL);
        assertThat(subAndUserInfo.getAuthorFullName()).isEqualTo(ADMIN_NAME);
    }

    @Test
    public void releaseSubmission() {
        submissionDao.releaseSubmission(1);
        Submission submission = submissionDao.getSubmissionByAccNo(SUB_ID);

        assertThat(submission.isReleased()).isTrue();
        assertThat(submissionDao.getAccessTags(1)).containsOnly(PUBLIC_TAG);
    }
}
