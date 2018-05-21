package uk.ac.ebi.biostd.webapp;

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
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.backend.configuration.TestConfiguration;
import uk.ac.ebi.biostd.backend.services.RemoteOperations;
import uk.ac.ebi.biostd.backend.testing.IntegrationTestUtil;
import uk.ac.ebi.biostd.backend.testing.ResourceHandler;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestConfiguration.class)
@DirtiesContext
public class PendingSubmissionApiTest {

    private static final String NEW_PAGETAB = "input/EMPTY.pagetab.json";

    @ClassRule
    public static TemporaryFolder TEST_FOLDER = new TemporaryFolder();

    @Autowired
    private TestRestTemplate restTemplate;

    private RemoteOperations operationsService;

    @BeforeClass
    public static void beforeAll() throws IOException {
        IntegrationTestUtil.initFileSystem(TEST_FOLDER);
    }

    @Before
    public void setup() {
        operationsService = new RemoteOperations(restTemplate);
    }


    @Test
    public void testCreatePendingSubmissionNoErrors() {
        String sessionId = operationsService.login("admin_user@ebi.ac.uk", "123456").getSessid();
        String data = ResourceHandler.getResourceFileAsString(NEW_PAGETAB).replaceAll("\\s+","");
        ResponseEntity<PendingSubmissionDto> submDto = restTemplate
                .postForEntity("/submissions/pending?BIOSTDSESS=" + sessionId, data, PendingSubmissionDto.class);

        PendingSubmissionDto dto = submDto.getBody();
        assertThat(dto).isNotNull();
        assertThat(dto.getAccno()).matches("TMP_.+");
        assertThat(dto.getChanged()).isGreaterThan(0);
        assertThat(dto.getData().toString()).isEqualTo(data);

        restTemplate.delete("/submissions/pending/" + dto.getAccno() + "?BIOSTDSESS=" + sessionId);
    }

    @Test
    public void testGetPendingSubmissionsWithoutParams() {
        String sessionId = operationsService.login("admin_user@ebi.ac.uk", "123456").getSessid();
        ResponseEntity<PendingSubmissionListDto> submListDto = restTemplate
                .getForEntity("/submissions/pending?BIOSTDSESS=" + sessionId, PendingSubmissionListDto.class);
        assertThat(submListDto).isNotNull();
        assertThat(submListDto.getBody().getSubmissions()).isEmpty();
    }
}
