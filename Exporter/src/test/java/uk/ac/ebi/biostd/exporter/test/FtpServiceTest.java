package uk.ac.ebi.biostd.exporter.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        "classpath:scripts/sql/public_submission.sql",
        "classpath:scripts/sql/public_file_list_submission.sql"})
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
        createOutdatedLinks();
        createSubmissionFolders();

        String basePath = folder.getRoot().getAbsolutePath();
        when(properties.getBaseFtpPath()).thenReturn(basePath + "/ftp");
        when(properties.getBaseBioStudiesPath()).thenReturn(basePath + "/submissions");
    }

    @Test
    public void generateFtpLinks() {
        testInstance.execute();

        assertTrue(ftpFileExists("S-EPMCxxx633/S-EPMC3343633/File1.txt"));
        assertTrue(ftpFileExists("S-EPMCxxx634/S-EPMC3343634/File2.txt"));
        assertFalse(ftpFileExists("S-EPMCxxx633/S-EPMC3343633/OutdatedFile1.txt"));
    }

    @Test
    public void generateFtpLinksByAccNo() {
        testInstance.execute("S-EPMC3343634");

        assertFalse(ftpFileExists("S-EPMCxxx633/S-EPMC3343633/File1.txt"));
        assertTrue(ftpFileExists("S-EPMCxxx634/S-EPMC3343634/File2.txt"));
        assertTrue(ftpFileExists("S-EPMCxxx633/S-EPMC3343633/OutdatedFile1.txt"));
    }

    private void createSubmissionFolders() throws IOException {
        folder.newFolder("submissions", "S-EPMC", "S-EPMCxxx633", "S-EPMC3343633", "Files");
        folder.newFolder("submissions", "S-EPMC", "S-EPMCxxx634", "S-EPMC3343634", "Files");

        folder.newFile("submissions/S-EPMC/S-EPMCxxx633/S-EPMC3343633/Files/File1.txt");
        folder.newFile("submissions/S-EPMC/S-EPMCxxx634/S-EPMC3343634/Files/File2.txt");
    }

    private void createOutdatedLinks() throws IOException {
        folder.newFolder("ftp", "S-EPMC", "S-EPMCxxx633", "S-EPMC3343633");
        folder.newFile("ftp/S-EPMC/S-EPMCxxx633/S-EPMC3343633/OutdatedFile1.txt");
    }

    private boolean ftpFileExists(String path) {
        return Files.exists(Paths.get(folder.getRoot().getAbsolutePath() + "/ftp/S-EPMC/" + path));
    }
}
