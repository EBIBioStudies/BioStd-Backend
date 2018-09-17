package uk.ac.ebi.biostd.exporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
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
import uk.ac.ebi.biostd.TestConfiguration;
import uk.ac.ebi.biostd.exporter.jobs.common.api.ExportPipeline;
import uk.ac.ebi.biostd.exporter.jobs.stats.StatsExport;
import uk.ac.ebi.biostd.exporter.jobs.stats.StatsJobsFactory;
import uk.ac.ebi.biostd.exporter.jobs.stats.StatsProperties;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
@Sql(scripts = {"classpath:scripts/sql/create_schema.sql", "classpath:scripts/sql/init-pmc.sql"})
public class StatsExportTest extends BaseIntegrationTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Autowired
    private StatsExport statsExport;

    @Autowired
    private StatsJobsFactory jobsFactory;

    @MockBean
    private StatsProperties properties;

    private ExportPipeline exportPipeline;

    @Before
    public void setup() throws IOException {
        exportPipeline = new ExportPipeline(1, ImmutableList.of(statsExport), jobsFactory);

        String basePath = folder.getRoot().getAbsolutePath();

        new File(basePath + "/submissions").mkdir();
        when(properties.getBasePath()).thenReturn(basePath + "/submissions");

        new File(basePath + "/updates").mkdir();
        when(properties.getOutFilePath()).thenReturn(basePath + "/updates/stats.csv");

        String pageTabFile = basePath + "/submissions/S-EPMC/S-EPMCxxx986/S-EPMC4273986/S-EPMC4273986.pagetab.tsv";
        FileUtils.copyFile(getResource("/test_files/S-EPMC4273986.tsv"), new File(pageTabFile));
    }

    @Test
    public void testStatsExport() throws Exception {
        exportPipeline.execute();
        File statsFile = new File(folder.getRoot().getAbsolutePath() + "/updates/stats.csv");
        assertThat(statsFile).exists();
    }

}

