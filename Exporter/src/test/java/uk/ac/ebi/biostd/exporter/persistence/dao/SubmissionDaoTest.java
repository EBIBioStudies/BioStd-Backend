package uk.ac.ebi.biostd.exporter.persistence.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.biostd.TestConfiguration;
import uk.ac.ebi.biostd.exporter.configuration.ExporterGeneralProperties;
import uk.ac.ebi.biostd.exporter.model.Submission;
import uk.ac.ebi.biostd.exporter.persistence.model.SubAndUserInfo;
import uk.ac.ebi.biostd.exporter.utils.DateUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@DirtiesContext
@ContextConfiguration(classes = TestConfiguration.class)
public class SubmissionDaoTest extends PersistenceTest {
    private static final String SUB_ID = "Test-Submission-01/01/2018";
    private static final String LIB_FILE_SUB_ID = "S-BIAD2";
    private static final String ADMIN_EMAIL = "biostudies-dev@ebi.ac.uk";
    private static final String ADMIN_NAME = "Biostudy manager";
    private static final String PUBLIC_TAG = "Public";

    @Autowired
    private SubmissionDao submissionDao;

    @MockBean
    private ExporterGeneralProperties exporterGeneralProperties;

    @Before
    public void setUp() {
        when(exporterGeneralProperties.getLibFileStudies()).thenReturn(Arrays.asList(LIB_FILE_SUB_ID));
    }

    @Test
    public void getPendingToReleaseSub() {
        List<SubAndUserInfo> infoResult = submissionDao.getPendingToReleaseSub(0, DateUtils.now().toEpochSecond());
        assertThat(infoResult).hasSize(2);
        assertResultInfo(infoResult.get(0));
        assertResultInfo(infoResult.get(1));
    }

    @Test
    public void releaseSubmission() {
        submissionDao.releaseSubmission(1);
        Submission submission = submissionDao.getSubmissionByAccNo(SUB_ID);

        assertThat(submission.isReleased()).isTrue();
        assertThat(submissionDao.getAccessTags(1)).containsOnly(PUBLIC_TAG);
    }

    @Test
    public void libraryFileSubmission() {
        Submission regularSubmission = submissionDao.getSubmissionByAccNo(SUB_ID);
        assertFalse(regularSubmission.isLibFileSubmission());

        Submission libFileSubmission = submissionDao.getSubmissionByAccNo(LIB_FILE_SUB_ID);
        assertTrue(libFileSubmission.isLibFileSubmission());
    }

    private void assertResultInfo(SubAndUserInfo subAndUserInfo) {
        assertThat(subAndUserInfo.getAuthorEmail()).isEqualTo(ADMIN_EMAIL);
        assertThat(subAndUserInfo.getAuthorFullName()).isEqualTo(ADMIN_NAME);
    }
}
