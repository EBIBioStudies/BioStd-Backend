package uk.ac.ebi.biostd.exporter.test;

import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import java.io.IOException;
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
import uk.ac.ebi.biostd.exporter.jobs.ftp.FtpPublisherProperties;
import uk.ac.ebi.biostd.exporter.jobs.ftp.FtpService;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
@Sql(scripts = {
        "classpath:scripts/sql/create_schema.sql",
        "classpath:scripts/sql/init-full-export.sql",
        "classpath:scripts/sql/private_submission.sql",
        "classpath:scripts/sql/public_submission.sql"})
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = { "classpath:scripts/sql/drop_schema.sql" })
public class FtpServiceTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Autowired
    private FtpService testInstance;

    @MockBean
    private FtpPublisherProperties properties;

    @Before
    public void setUp() throws IOException {
        folder.newFolder("ftp");
        createSubmissionFolders();

        String basePath = folder.getRoot().getAbsolutePath();
        when(properties.getBaseFtpPath()).thenReturn(basePath + "/ftp");
        when(properties.getBaseBioStudiesPath()).thenReturn(basePath + "/submissions");
    }

    @Test
    public void generateFtpLinks() {
        testInstance.execute();
    }

    private void createSubmissionFolders() throws IOException {
        folder.newFolder("submissions");

        folder.newFolder("submissions", "S-EPMC");
        folder.newFolder("submissions", "S-EPMC", "S-EPMCxxx633");
        folder.newFolder("submissions", "S-EPMC", "S-EPMCxxx633", "S-EPMC3343633");
        folder.newFolder("submissions", "S-EPMC", "S-EPMCxxx633", "S-EPMC3343633", "Files");

        folder.newFile("submissions/S-EPMC/S-EPMCxxx633/S-EPMC3343633/Files/File1.txt");
    }
}
