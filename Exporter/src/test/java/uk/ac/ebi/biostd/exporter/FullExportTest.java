package uk.ac.ebi.biostd.exporter;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import com.google.common.collect.ImmutableList;
import java.io.File;
import javax.xml.transform.Source;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import uk.ac.ebi.biostd.TestConfiguration;
import uk.ac.ebi.biostd.exporter.jobs.common.api.ExportPipeline;
import uk.ac.ebi.biostd.exporter.jobs.full.FullExport;
import uk.ac.ebi.biostd.exporter.jobs.full.FullExportJobProperties;
import uk.ac.ebi.biostd.exporter.jobs.full.FullJobJobsFactory;
import uk.ac.ebi.biostd.exporter.schedulers.JobsScheduler;
import uk.ac.ebi.biostd.exporter.utils.FileUtil;
import uk.ac.ebi.biostd.test.util.JsonComparator;
import uk.ac.ebi.biostd.test.util.XmlAttributeFilter;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
@Sql(scripts = {"classpath:create_schema.sql", "classpath:init-full-export.sql"}, executionPhase = BEFORE_TEST_METHOD)
public class FullExportTest extends BaseIntegrationTest {

    private static final String[] IGNORED_FIELDS = new String[]{"rtime", "ctime", "mtime", "@startTimeTS", "@endTimeTS",
            "@startTime", "@endTime", "@elapsedTime"};

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ExportPipeline exportPipeline;

    @MockBean
    private JobsScheduler jobsScheduler;

    @MockBean
    private FullExportJobProperties exportProperties;

    @Autowired
    private FullExport fullExport;

    @Autowired
    private FullJobJobsFactory fullJobsFactory;

    @Before
    public void setup() {
        exportPipeline = new ExportPipeline(1, ImmutableList.of(fullExport), fullJobsFactory);

        when(exportProperties.getFilePath()).thenReturn(folder.getRoot().getAbsolutePath() + "/");
        when(exportProperties.getFileName()).thenReturn("studies");
        when(exportProperties.getWorkers()).thenReturn(1);
        when(exportProperties.getQueryModified()).thenReturn(EMPTY);
        when(exportProperties.getNotificationUrl()).thenReturn("http://localhost:8181/api/update/full");
    }

    @Test
    public void testFullExport() {
        exportPipeline.execute();

        File[] files = folder.getRoot().listFiles();
        assertThat(files).hasSize(2);
        assertThatJsonFile(files[0]);
        assertXmlFile(files[1]);
    }

    private void assertXmlFile(File file) {
        Source control = Input.fromFile(getResource("/test_files/full.xml").getAbsolutePath()).build();
        String content = FileUtil.readFile(file.getAbsolutePath());
        Diff diff = DiffBuilder.compare(control).withTest(Input.fromString(content))
                .ignoreWhitespace()
                .ignoreComments()
                .withAttributeFilter(new XmlAttributeFilter(IGNORED_FIELDS)).build();
        assertThat(diff.hasDifferences()).isFalse();
    }

    private void assertThatJsonFile(File file) {
        String content = FileUtil.readFile(file.getAbsolutePath());
        String expected = FileUtil.readFile(getResource("/test_files/full.json").getAbsolutePath());
        assertEquals(expected, content, new JsonComparator(NON_EXTENSIBLE, IGNORED_FIELDS));
    }
}
