package uk.ac.ebi.biostd.webapp;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.biostd.webapp.application.configuration.ConfigProperties.CONFIG_FILE_LOCATION_VAR;
import static uk.ac.ebi.biostd.webapp.server.config.ConfigurationManager.BIOSTUDY_BASE_DIR;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biostd.backend.configuration.TestConfiguration;
import uk.ac.ebi.biostd.backend.model.SubmissionResult;
import uk.ac.ebi.biostd.backend.services.RemoteOperations;
import uk.ac.ebi.biostd.backend.services.ResourceHandler;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestConfiguration.class)
@Sql(scripts = {"classpath:init_data.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class BioStdApplicationTest {

    private static final String SUBMISSION_FILE = "input/S-BSST56.pagetab_for_test.xlsx";

    @ClassRule
    public static TemporaryFolder TEST_FOLDER = new TemporaryFolder();
    private static String NFS_PATH;

    @Autowired
    private ResourceHandler resourceHandler;

    @Autowired
    private RestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private RemoteOperations operationsService;

    @BeforeClass
    public static void beforeAll() throws IOException {
        NFS_PATH = TEST_FOLDER.getRoot().getPath();
        System.setProperty(BIOSTUDY_BASE_DIR, NFS_PATH);
        System.setProperty(CONFIG_FILE_LOCATION_VAR, NFS_PATH + "/config.properties");

        FileUtils.copyFile(
                new ClassPathResource("nfs/config.properties").getFile(),
                new File(NFS_PATH + "/config.properties"));
    }

    @Before
    public void setup() {
        operationsService = new RemoteOperations(restTemplate, port);
    }

    @Test
    public void testCreateBasicPageTabSubmission() {
        operationsService.refreshCache();
        String sessionId = operationsService.login("admin_user@ebi.ac.uk", "123456").getSessid();

        File submissionFile = resourceHandler.getResourceFile(SUBMISSION_FILE);
        SubmissionResult submissionResult = operationsService.createOrSubmit(sessionId, submissionFile);

        assertThat(submissionResult.getStatus()).isEqualTo("OK");

        assertFile("basic/S-ACC-TEST.json", NFS_PATH + "/submission/S-ACC-TEST/S-ACC-TEST.json");
        assertFile("basic/S-ACC-TEST.pagetab.tsv", NFS_PATH + "/submission/S-ACC-TEST/S-ACC-TEST.pagetab.tsv");
        assertFile("basic/S-ACC-TEST.xml", NFS_PATH + "/submission/S-ACC-TEST/S-ACC-TEST.xml");
    }

    private void assertFile(String expectedPath, String resultFilePath) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String expectedFile = resourceHandler.readResource(expectedPath)
                .replace("${TODAY}", dateFormat.format(new Date()));
        String resultFile = resourceHandler.readFile(resultFilePath);
        assertThat(resultFile).isEqualTo(expectedFile);
    }
}
