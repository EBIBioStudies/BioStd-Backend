package uk.ac.ebi.biostd.exporter.jobs.partial;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biostd.exporter.model.Submission;
import uk.ac.ebi.biostd.exporter.service.SubmissionService;
import uk.ac.ebi.biostd.exporter.utils.FileUtil;

@RunWith(MockitoJUnitRunner.class)
public class PartialSubmissionExporterTest {

    private static final String ACCNO = "acNo-123";
    private static final String FILE = "partial_update";
    private static final String FILE_CONTENT = "partial update file content";
    private static final String NOTIFICATION = "http://frontend.com/";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private PartialExportJobProperties mockProperties;

    @Mock
    private ObjectMapper mockObjectMapper;

    @Mock
    private SubmissionService mockSubService;

    @Mock
    private RestTemplate mockRestTemplate;

    private Submission updatedSubmission = new Submission();

    @InjectMocks
    private PartialSubmissionExporter testInstance;

    @Before
    public void setup() throws Exception {
        updatedSubmission.setAccno(ACCNO);

        when(mockSubService.getUpdatedSubmissions(anyLong())).thenReturn(singletonList(updatedSubmission));
        when(mockSubService.getDeletedSubmissions(anyLong())).thenReturn(singletonList(ACCNO));
        when(mockProperties.getFilePath()).thenReturn(folder.getRoot().getAbsolutePath() + "/");
        when(mockProperties.getFileName()).thenReturn(FILE);
        when(mockProperties.getNotificationUrl()).thenReturn(NOTIFICATION);
        when(mockObjectMapper.writeValueAsString(any(PartialUpdateFile.class))).thenReturn(FILE_CONTENT);
    }

    @Test
    public void execute() throws Exception {
        testInstance.execute();

        List<File> files = FileUtil.listFiles(mockProperties.getFilePath());
        assertThat(files).hasSize(1);

        File partialFile = files.get(0);
        assertThat(FileUtils.readFileToString(partialFile, StandardCharsets.UTF_8)).contains(FILE_CONTENT);
        verify(mockRestTemplate).getForEntity(NOTIFICATION + partialFile.getName(), String.class);
    }
}
