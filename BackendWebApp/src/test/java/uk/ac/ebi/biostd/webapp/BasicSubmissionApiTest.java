package uk.ac.ebi.biostd.webapp;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.backend.configuration.TestConfiguration;
import uk.ac.ebi.biostd.backend.model.SubmissionResult;
import uk.ac.ebi.biostd.backend.services.RemoteOperations;
import uk.ac.ebi.biostd.backend.testing.IntegrationTestUtil;
import uk.ac.ebi.biostd.backend.testing.ResourceHandler;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestConfiguration.class)
@DirtiesContext
public class BasicSubmissionApiTest {

    private static final String SUBMISSION_XLSX_FILE = "input/S-BSST56.pagetab_for_test.xlsx";
    private static final String SUBMISSION_JSON_FILE = "input/S-ACC-TEST.json";

    @ClassRule
    public static TemporaryFolder TEST_FOLDER = new TemporaryFolder();

    private static String NFS_PATH;

    @Autowired
    private TestRestTemplate restTemplate;

    private RemoteOperations operationsService;

    @BeforeClass
    public static void beforeAll() throws Exception {
        NFS_PATH = IntegrationTestUtil.initFileSystem(TEST_FOLDER);
    }

    @Before
    public void setup() {
        operationsService = new RemoteOperations(restTemplate);
    }

    @Test
    public void testCreateBasicPageTabSubmission() {
        String sessionId = operationsService.login("admin_user@ebi.ac.uk", "123456").getSessid();

        File submissionFile = ResourceHandler.getResourceFile(SUBMISSION_XLSX_FILE);
        SubmissionResult submissionResult = operationsService.createFileSubmission(sessionId, submissionFile);

        assertThat(submissionResult.getStatus()).isEqualTo("OK");
        assertSubmissionsOutput("XLSX");
    }

    @Test
    public void testCreateBasicJsonSubmission() {
        String sessionId = operationsService.login("admin_user@ebi.ac.uk", "123456").getSessid();
        String jsonPayload = ResourceHandler.getResourceFileAsString(SUBMISSION_JSON_FILE);

        SubmissionResult submissionResult = operationsService.createJsonSubmission(sessionId, jsonPayload);
        assertThat(submissionResult.getStatus()).isEqualTo("OK");
        assertSubmissionsOutput("JSON");
    }

    private void assertSubmissionsOutput(String postFix) {
        String accNo = "S-ACC-TEST-" + postFix;
        String basePath = format("%s/submission/%s/%s", NFS_PATH, accNo, accNo);

        assertFile("basic/S-ACC-TEST.json", basePath + ".json", accNo);
        assertFile("basic/S-ACC-TEST.pagetab.tsv", basePath + ".pagetab.tsv", accNo);
        assertFile("basic/S-ACC-TEST.xml", basePath + ".xml", accNo);
    }

    private void assertFile(String expectedPath, String resultFilePath, String accNo) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String expectedFile = ResourceHandler.getResourceFileAsString(expectedPath);
        expectedFile = expectedFile.replace("${TODAY}", dateFormat.format(new Date()));
        expectedFile = expectedFile.replace("${SUB_ACC_NO}", accNo);

        String resultFile = ResourceHandler.readFile(resultFilePath);
        assertThat(resultFile).isEqualToIgnoringWhitespace(expectedFile);
    }
}
