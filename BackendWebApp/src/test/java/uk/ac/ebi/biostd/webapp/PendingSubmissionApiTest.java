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
import org.springframework.http.HttpStatus;
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
    public void testCreatePendingSubmission() {
        String sessionId = login();

        String data = ResourceHandler.getResourceFileAsString(NEW_PAGETAB).replaceAll("\\s+", "");
        ResponseEntity<PendingSubmissionDto> response = restTemplate
                .postForEntity("/submissions/pending?BIOSTDSESS=" + sessionId, data, PendingSubmissionDto.class);

        PendingSubmissionDto dto = response.getBody();
        assertThat(dto).isNotNull();
        assertThat(dto.getAccno()).matches("TMP_.+");
        assertThat(dto.getChanged()).isGreaterThan(0);
        assertThat(dto.getData().toString()).isEqualTo(data);

        response = restTemplate.getForEntity("/submissions/pending/" + dto.getAccno() + "?BIOSTDSESS=" + sessionId, PendingSubmissionDto.class);
        PendingSubmissionDto dtoCopy = response.getBody();
        assertThat(dtoCopy).isNotNull();
        assertThat(dtoCopy.getAccno()).isEqualTo(dto.getAccno());
        assertThat(dtoCopy.getChanged()).isEqualTo(dto.getChanged());
        assertThat(dtoCopy.getData().toString()).isEqualTo(dto.getData().toString());

        restTemplate.delete("/submissions/pending/" + dto.getAccno() + "?BIOSTDSESS=" + sessionId);
    }

    @Test
    public void testCreatePendingSubmissionWithErrors() {
        ResponseEntity<PendingSubmissionDto> response = restTemplate
                .postForEntity("/submissions/pending?BIOSTDSESS=" + login(), "blah blah", PendingSubmissionDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void testGetNoneExistedSubmission() {
        ResponseEntity<PendingSubmissionDto> response = restTemplate.getForEntity("/submissions/pending/1234?BIOSTDSESS=" + login(), PendingSubmissionDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void testUpdatePendingSubmission() {
        String sessionId = login();
        String data = ResourceHandler.getResourceFileAsString(NEW_PAGETAB).replaceAll("\\s+", "");
        ResponseEntity<PendingSubmissionDto> createResponse = restTemplate
                .postForEntity("/submissions/pending?BIOSTDSESS=" + sessionId, data, PendingSubmissionDto.class);

        PendingSubmissionDto dto1 = createResponse.getBody();
        long mTime1 = dto1.getChanged();

        ResponseEntity<PendingSubmissionDto> updateResponse = restTemplate
                .postForEntity("/submissions/pending/" + dto1.getAccno() + "?BIOSTDSESS=" + sessionId, dto1, PendingSubmissionDto.class);

        PendingSubmissionDto dto2 = updateResponse.getBody();
        long mTime2 = dto2.getChanged();

        assertThat(mTime1).isLessThan(mTime2);

        restTemplate.delete("/submissions/pending/" + dto1.getAccno() + "?BIOSTDSESS=" + sessionId);
    }


    @Test
    public void testGetPendingSubmissionsWithoutParams() {
        ResponseEntity<PendingSubmissionListDto> response = restTemplate
                .getForEntity("/submissions/pending?BIOSTDSESS=" + login(), PendingSubmissionListDto.class);
        assertThat(response).isNotNull();
        assertThat(response.getBody().getSubmissions()).isEmpty();
    }

    private String login() {
        return operationsService.login("admin_user@ebi.ac.uk", "123456").getSessid();
    }
}
