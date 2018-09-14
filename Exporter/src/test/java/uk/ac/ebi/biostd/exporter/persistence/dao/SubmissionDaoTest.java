package uk.ac.ebi.biostd.exporter.persistence.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.After;
import org.junit.Before;
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

    @Autowired
    private SubmissionDao submissionDao;

    @Before
    public void setup() {
    }

    @After
    public void teardown() {
    }

    @Test
    public void getPendingToReleaseSub() {
        List<SubAndUserInfo> infoResult = submissionDao.getPendingToReleaseSub(0, DateUtils.now().toEpochSecond());
        assertThat(infoResult).hasSize(1);
        assertResultInfo(infoResult.get(0));
    }

    private void assertResultInfo(SubAndUserInfo subAndUserInfo) {
        assertThat(subAndUserInfo.getAuthorEmail()).isEqualTo("biostudies-dev@ebi.ac.uk");
        assertThat(subAndUserInfo.getAuthorFullName()).isEqualTo("Biostudy manager");
    }

    @Test
    public void releaseSubmission() {
        submissionDao.releaseSubmission(1);
        Submission submission = submissionDao.getSubmissionByAccNo(SUB_ID);

        assertThat(submission.isReleased()).isTrue();
        assertThat(submissionDao.getAccessTags(1)).containsOnly("Public");
    }
}
