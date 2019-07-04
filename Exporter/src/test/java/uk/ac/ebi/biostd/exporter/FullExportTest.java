package uk.ac.ebi.biostd.exporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.TestConfiguration;
import uk.ac.ebi.biostd.exporter.jobs.common.api.ExportPipeline;
import uk.ac.ebi.biostd.exporter.jobs.full.FullExport;
import uk.ac.ebi.biostd.exporter.jobs.full.FullJobJobsFactory;
import uk.ac.ebi.biostd.exporter.jobs.full.configuration.FullExportAllSubmissionsProperties;
import uk.ac.ebi.biostd.exporter.jobs.full.configuration.FullExportJobProperties;
import uk.ac.ebi.biostd.exporter.jobs.full.configuration.FullExportPublicOnlySubmissionsProperties;
import uk.ac.ebi.biostd.exporter.utils.FileUtil;
import uk.ac.ebi.biostd.test.util.JsonComparator;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
@Sql(scripts = {
        "classpath:scripts/sql/create_schema.sql",
        "classpath:scripts/sql/init-full-export.sql",
        "classpath:scripts/sql/private_submission.sql",
        "classpath:scripts/sql/public_submission.sql",
        "classpath:scripts/sql/public_file_list_submission.sql"})
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"classpath:scripts/sql/drop_schema.sql"})
public class FullExportTest extends BaseIntegrationTest {

    private static final String FULL_EXPORT_NAME = "studies";
    private static final String PUBLIC_ONLY_EXPORT_NAME = "publicOnlyStudies";
    private static final String NOTIFICATION_URL = "http://localhost:8181/api/update/full";
    private static final String EXPECTED_OUTPUT_PATH = "/test_files/";
    private static final String EXPECTED_FULL_JSON_PATH = EXPECTED_OUTPUT_PATH + "full.json";
    private static final String EXPECTED_PUBLIC_ONLY_JSON_PATH = EXPECTED_OUTPUT_PATH + "publicOnly.json";
    private static final String[] IGNORED_FIELDS = new String[]{"rtime", "ctime", "mtime", "@startTimeTS", "@endTimeTS",
            "@startTime", "@endTime", "@elapsedTime"};

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ExportPipeline exportPipeline;

    @MockBean
    private FullExportJobProperties exportProperties;

    @Mock
    private FullExportAllSubmissionsProperties allSubmissionsProperties;

    @Mock
    private FullExportPublicOnlySubmissionsProperties publicOnlySubmissionsProperties;

    @Autowired
    private FullExport fullExport;

    @Autowired
    private FullJobJobsFactory fullJobsFactory;

    @Before
    public void setup() {
        exportPipeline = new ExportPipeline(1, ImmutableList.of(fullExport), fullJobsFactory);

        when(allSubmissionsProperties.getFilePath()).thenReturn(folder.getRoot().getAbsolutePath() + "/");
        when(allSubmissionsProperties.getFileName()).thenReturn(FULL_EXPORT_NAME);
        when(publicOnlySubmissionsProperties.getFilePath()).thenReturn(folder.getRoot().getAbsolutePath() + "/");
        when(publicOnlySubmissionsProperties.getFileName()).thenReturn(PUBLIC_ONLY_EXPORT_NAME);
        when(exportProperties.getAllSubmissions()).thenReturn(allSubmissionsProperties);
        when(exportProperties.getPublicOnlySubmissions()).thenReturn(publicOnlySubmissionsProperties);
        when(exportProperties.getWorkers()).thenReturn(1);
        when(exportProperties.getNotificationUrl()).thenReturn("http://localhost:8181/api/update/full");
        when(exportProperties.getNotificationUrl()).thenReturn(NOTIFICATION_URL);
    }

    @Test
    public void testFullExport() {
        assertExportJobResults(EXPECTED_FULL_JSON_PATH);
    }

    private void assertExportJobResults(String expectedFullJson) {
        exportPipeline.execute();

        File[] files = folder.getRoot().listFiles();
        assertThat(files).hasSize(2);

        Arrays.sort(files, Comparator.comparing(File::getName));
        assertThatJsonFile(files[0], EXPECTED_PUBLIC_ONLY_JSON_PATH);
        assertThatJsonFile(files[1], expectedFullJson);
    }


    private void assertThatJsonFile(File file, String expectedFilePath) {
        String content = FileUtil.readFile(file.getAbsolutePath());
        String expected = FileUtil.readFile(getResource(expectedFilePath).getAbsolutePath());
        assertEquals(expected, content, new JsonComparator(NON_EXTENSIBLE, IGNORED_FIELDS));
    }
}
