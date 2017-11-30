package uk.ac.ebi.biostd.webapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.backend.configuration.TestConfiguration;
import uk.ac.ebi.biostd.backend.configuration.WebConfiguration;
import uk.ac.ebi.biostd.backend.model.SubmissionResult;
import uk.ac.ebi.biostd.backend.services.RemoteOperations;
import uk.ac.ebi.biostd.backend.services.ResourceHandler;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Import({WebConfiguration.class, TestConfiguration.class})
public class BioStdApplicationTest {

    private static final String NFS_PATH = "/home/jcamilorada/Projects/BioStudies/NFS";
    private static final String SUBMISSION_FILE = "input/S-BSST56.pagetab_for_test.xlsx";

    @Autowired
    private RemoteOperations operationsService;

    @Autowired
    private ResourceHandler resourceHandler;

    @Test
    public void testCreateBasicSubmission() {
        String sessionId = operationsService.login("jcamilorada@gmail.com", "123456").getSessid();

        File submissionFile = resourceHandler.getResourceFile(SUBMISSION_FILE);
        SubmissionResult submissionResult = operationsService.createOrSubmit(sessionId, submissionFile);

        assertThat(submissionResult.getStatus()).isEqualTo("OK");

        assertFile("basic/S-ACC-TEST.json", NFS_PATH + "/submission/S-ACC-TEST/S-ACC-TEST.json");
        assertFile("basic/S-ACC-TEST.pagetab.tsv", NFS_PATH + "/submission/S-ACC-TEST/S-ACC-TEST.pagetab.tsv");
        assertFile("basic/S-ACC-TEST.xml", NFS_PATH + "/submission/S-ACC-TEST/S-ACC-TEST.xml");
    }

    private void assertFile(String expectedResourcePath, String resultFilePath) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String expectedFile = resourceHandler.readResource(expectedResourcePath)
                .replace("${TODAY}", dateFormat.format(new Date()));
        String resultFile = resourceHandler.readFile(resultFilePath);
        assertThat(resultFile).isEqualTo(expectedFile);
    }
}
