package uk.ac.ebi.biostd.exporter.jobs.full.job;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import org.easybatch.core.job.JobMetrics;
import org.easybatch.core.job.JobReport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ebi.biostd.exporter.commons.FileUtils;

/**
 * Unit test for {@link FileUpdater}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FileUpdaterTest {

    private static final long WRITE_COUNT = 2L;
    private static final String FILE_NAME = "file_name";

    @Mock
    private JobReport jobReport;

    @Mock
    private JobMetrics metrics;

    @Mock
    private FileUtils fileUtil;

    @Before
    public void setup() {
        when(jobReport.getMetrics()).thenReturn(metrics);
        when(metrics.getWriteCount()).thenReturn(WRITE_COUNT);
    }

    @Test
    public void afterJobEndWhenLessThanThreshold() {
        new FileUpdater(fileUtil, FILE_NAME, 4L).afterJobEnd(jobReport);

        verifyZeroInteractions(fileUtil);
    }

    @Test
    public void afterJobEndWhenPassThreshold() {
        new FileUpdater(fileUtil, FILE_NAME, 2L).afterJobEnd(jobReport);

        verify(fileUtil).copy(Paths.get("file_name_tmp"), Paths.get(FILE_NAME), REPLACE_EXISTING);
        verify(fileUtil).delete(Paths.get("file_name_tmp"));
    }
}
