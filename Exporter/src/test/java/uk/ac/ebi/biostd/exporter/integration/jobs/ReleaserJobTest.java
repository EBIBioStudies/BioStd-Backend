package uk.ac.ebi.biostd.exporter.jobs.releaser;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ebi.biostd.exporter.persistence.dao.SubmissionDao;
import uk.ac.ebi.biostd.exporter.persistence.model.SubAndUserInfo;
import uk.ac.ebi.biostd.exporter.utils.DateUtils;

/**
 * Unit test for {@link ReleaserJob}
 */
@RunWith(MockitoJUnitRunner.class)
public class ReleaserJobTest {

    private static final long SUB_ID = 44L;

    @Mock
    private SubmissionDao mockSubmissionDao;

    @Captor
    private ArgumentCaptor<Long> longCaptor;

    private SubAndUserInfo subAndUserInfo;

    @InjectMocks
    private ReleaserJob testInstance;

    @Before
    public void setup() {
        subAndUserInfo = new SubAndUserInfo();
        subAndUserInfo.setSubId(SUB_ID);

        when(mockSubmissionDao.getPendingToReleaseSub(eq(0L), anyLong())).thenReturn(singletonList(subAndUserInfo));
    }

    @Test
    public void execute() {
        testInstance.execute();

        verify(mockSubmissionDao).releaseSubmission(SUB_ID);
        verify(mockSubmissionDao).getPendingToReleaseSub(eq(0L), longCaptor.capture());

        Long toEpochSeconds = longCaptor.getValue();
        assertThat(toEpochSeconds).isCloseTo(DateUtils.now().toEpochSecond(), offset(10L));
    }
}
