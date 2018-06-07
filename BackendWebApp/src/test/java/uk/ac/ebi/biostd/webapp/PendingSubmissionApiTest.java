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
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.backend.configuration.TestConfiguration;
import uk.ac.ebi.biostd.backend.services.RemoteOperations;
import uk.ac.ebi.biostd.backend.testing.IntegrationTestUtil;
import uk.ac.ebi.biostd.backend.testing.ResourceHandler;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListDto;

import java.io.IOException;

import static java.lang.String.format;
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
                .postForEntity(format("/submissions/pending?BIOSTDSESS=%s", sessionId),
                        new HttpEntity<>(data, headers()), PendingSubmissionDto.class);

        PendingSubmissionDto dto = response.getBody();
        assertThat(dto).isNotNull();
        assertThat(dto.getAccno()).matches("TMP_.+");
        assertThat(dto.getChanged()).isGreaterThan(0);
        assertThat(dto.getData().toString()).isEqualTo(data);

        response = restTemplate
                .getForEntity(format("/submissions/pending/%s?BIOSTDSESS=%s", dto.getAccno(), sessionId), PendingSubmissionDto.class);

        PendingSubmissionDto dtoCopy = response.getBody();
        assertThat(dtoCopy).isNotNull();
        assertThat(dtoCopy.getAccno()).isEqualTo(dto.getAccno());
        assertThat(dtoCopy.getChanged()).isEqualTo(dto.getChanged());
        assertThat(dtoCopy.getData().toString()).isEqualTo(dto.getData().toString());

        restTemplate.delete(format("/submissions/pending/%s?BIOSTDSESS=%s", dto.getAccno(), sessionId));
    }

    @Test
    public void testCreatePendingSubmissionBadRequest() {
        ResponseEntity<PendingSubmissionDto> response = restTemplate
                .postForEntity(format("/submissions/pending?BIOSTDSESS=%s", login()),
                        new HttpEntity<>("not a json data", headers()), PendingSubmissionDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testGetNoneExistedSubmission() {
        ResponseEntity<PendingSubmissionDto> response = restTemplate
                .getForEntity(format("/submissions/pending/1234?BIOSTDSESS=%s", login()), PendingSubmissionDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void testUpdatePendingSubmission() {
        String sessionId = login();
        String data = ResourceHandler.getResourceFileAsString(NEW_PAGETAB).replaceAll("\\s+", "");
        ResponseEntity<PendingSubmissionDto> createResponse = restTemplate
                .postForEntity(format("/submissions/pending?BIOSTDSESS=%s", sessionId),
                        new HttpEntity<>(data, headers()), PendingSubmissionDto.class);

        PendingSubmissionDto dto1 = createResponse.getBody();
        long mTime1 = dto1.getChanged();

        ResponseEntity<PendingSubmissionDto> updateResponse = restTemplate
                .postForEntity(format("/submissions/pending/%s?BIOSTDSESS=%s", dto1.getAccno(), sessionId),
                        new HttpEntity<>(dto1.getData(), headers()), PendingSubmissionDto.class);

        PendingSubmissionDto dto2 = updateResponse.getBody();
        long mTime2 = dto2.getChanged();

        assertThat(mTime1).isLessThan(mTime2);

        delete(dto1.getAccno(), sessionId);
    }

    @Test
    public void testUpdatePendingSubmissionBadRequest() {
        String sessionId = login();

        ResponseEntity<PendingSubmissionDto> updateResponse = restTemplate
                .postForEntity(format("/submissions/pending/111?BIOSTDSESS=%s", sessionId),
                        new HttpEntity<>("not a json data", headers()), PendingSubmissionDto.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testGetAllPendingSubmissions() {
        ResponseEntity<PendingSubmissionListDto> response = restTemplate
                .getForEntity(format("/submissions/pending?BIOSTDSESS=%s", login()), PendingSubmissionListDto.class);
        assertThat(response).isNotNull();
        assertThat(response.getBody().getSubmissions()).isEmpty();
    }

    private String login() {
        return operationsService.login("admin_user@ebi.ac.uk", "123456").getSessid();
    }

    private void delete(String accno, String sessionId) {
        restTemplate.delete(format("/submissions/pending/%s?BIOSTDSESS=%s", accno, sessionId));
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
