package uk.ac.ebi.biostd.exporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
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
@Sql(scripts = {
        "classpath:scripts/sql/create_schema.sql",
        "classpath:scripts/sql/init-full-export.sql",
        "classpath:scripts/sql/private_submission.sql",
        "classpath:scripts/sql/public_submission.sql"})
public class StatsExportTest extends BaseIntegrationTest {
    private static final String EXPECTED_STATS_1 = "\"S-EPMC2873748\",\"0\",\"1\",\"34984\",\"true\"";
    private static final String EXPECTED_STATS_2 = "\"S-EPMC3343633\",\"0\",\"1\",\"9170139\",\"false\"";

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
    public void setup() {
        exportPipeline = new ExportPipeline(1, ImmutableList.of(statsExport), jobsFactory);

        String basePath = folder.getRoot().getAbsolutePath();

        new File(basePath + "/submissions").mkdir();
        when(properties.getBasePath()).thenReturn(basePath + "/submissions");

        new File(basePath + "/updates").mkdir();
        when(properties.getOutFilePath()).thenReturn(basePath + "/updates/stats.csv");
        when(properties.getImagingProjects()).thenReturn(Arrays.asList("BioImages", "BioVideos"));
    }

    @Test
    public void testStatsExport() throws Exception {
        exportPipeline.execute();
        File statsFile = new File(folder.getRoot().getAbsolutePath() + "/updates/stats.csv");
        assertThat(statsFile).exists();
        assertStats(Files.readLines(statsFile, Charset.defaultCharset()), EXPECTED_STATS_1, EXPECTED_STATS_2);
    }

    private void assertStats(List<String> stats, String... expectedStats) {
        assertThat(stats.size()).isEqualTo(expectedStats.length);

        for (int index = 0; index < stats.size(); index++) {
            assertThat(stats.get(index)).isEqualTo(expectedStats[index]);
        }
    }
}
