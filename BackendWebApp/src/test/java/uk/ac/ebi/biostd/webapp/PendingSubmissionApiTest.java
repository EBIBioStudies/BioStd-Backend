package uk.ac.ebi.biostd.webapp;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.*;
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
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmissionReportDto;

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
    private String sessionId;

    @BeforeClass
    public static void beforeAll() throws IOException {
        IntegrationTestUtil.initFileSystem(TEST_FOLDER);
    }

    @Before
    public void setUp() {
        operationsService = new RemoteOperations(restTemplate);
        sessionId = login();
    }

    @After
    public void tearDown() {
        getAllPendingSubmissions(sessionId).getBody().getSubmissions()
                .forEach(subm -> deletePendingSubmission(subm.getAccno(), sessionId));
    }

    @Test
    public void testCreatePendingSubmission() {
        String sessionId = login();

        String data = getSubmissionSample();

        PendingSubmissionDto dto = createPendingSubmission(data, sessionId).getBody();

        assertThat(dto).isNotNull();
        assertThat(dto.getAccno()).matches("TMP_.+");
        assertThat(dto.getChanged()).isGreaterThan(0);
        assertThat(dto.getData().toString()).isEqualTo(data);

        PendingSubmissionDto dtoCopy = getPendingSubmission(dto.getAccno(), sessionId).getBody();

        assertThat(dtoCopy).isNotNull();
        assertThat(dtoCopy.getAccno()).isEqualTo(dto.getAccno());
        assertThat(dtoCopy.getChanged()).isEqualTo(dto.getChanged());
        assertThat(dtoCopy.getData().toString()).isEqualTo(dto.getData().toString());
    }

    @Test
    public void testCreatePendingSubmissionBadRequest() {
        ResponseEntity<PendingSubmissionDto> response = createPendingSubmission("not a json data", login());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testGetNoneExistedSubmission() {
        ResponseEntity<PendingSubmissionDto> response = getPendingSubmission("12345", login());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void testUpdatePendingSubmission() {
        String sessionId = login();
        String data = getSubmissionSample();

        PendingSubmissionDto dto1 = createPendingSubmission(data, sessionId).getBody();
        long mTime1 = dto1.getChanged();

        PendingSubmissionDto dto2 = updatePendingSubmission(dto1.getAccno(), data, sessionId).getBody();
        long mTime2 = dto2.getChanged();

        assertThat(mTime1).isLessThan(mTime2);
    }

    @Test
    public void testUpdatePendingSubmissionBadRequest() {
        ResponseEntity<PendingSubmissionDto> response = updatePendingSubmission("111", "not a json data", login());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testGetAllPendingSubmissions() {
        PendingSubmissionListDto dto = restTemplate
                .getForEntity(format("/submissions/pending?BIOSTDSESS=%s", login()), PendingSubmissionListDto.class)
                .getBody();

        assertThat(dto.getSubmissions()).isEmpty();
    }

    @Test
    public void testSubmitNewPendingSubmission() {
        String sessionId = login();
        PendingSubmissionDto dto = createPendingSubmission(getSubmissionSample(), sessionId).getBody();

        ResponseEntity<SubmissionReportDto> response = submitPendingSubmission(dto.getAccno(), sessionId);

        assertThat(response.getBody().getStatus()).isEqualTo("OK");
    }

    private String getSubmissionSample() {
        return ResourceHandler.getResourceFileAsString(NEW_PAGETAB).replaceAll("\\s+", "");
    }

    private String login() {
        return operationsService.login("admin_user@ebi.ac.uk", "123456").getSessid();
    }

    private void deletePendingSubmission(String accno, String sessionId) {
        restTemplate.delete(format("/submissions/pending/%s?BIOSTDSESS=%s", accno, sessionId));
    }

    private ResponseEntity<PendingSubmissionDto> createPendingSubmission(String data, String sessionId) {
        return restTemplate
                .postForEntity(format("/submissions/pending?BIOSTDSESS=%s", sessionId),
                        new HttpEntity<>(data, headers()), PendingSubmissionDto.class);
    }

    private ResponseEntity<PendingSubmissionDto> updatePendingSubmission(String accno, String data, String sessionId) {
        return restTemplate
                .postForEntity(format("/submissions/pending/%s?BIOSTDSESS=%s", accno, sessionId),
                        new HttpEntity<>(data, headers()), PendingSubmissionDto.class);
    }

    private ResponseEntity<SubmissionReportDto> submitPendingSubmission(String accno, String sessionId) {
        return restTemplate
                .postForEntity(format("/submissions/pending/%s/submit?BIOSTDSESS=%s", accno, sessionId),
                        HttpEntity.EMPTY, SubmissionReportDto.class);
    }

    private ResponseEntity<PendingSubmissionDto> getPendingSubmission(String accno, String sessionId) {
        return restTemplate
                .getForEntity(format("/submissions/pending/%s?BIOSTDSESS=%s", accno, sessionId), PendingSubmissionDto.class);
    }

    private ResponseEntity<PendingSubmissionListDto> getAllPendingSubmissions(String sessionId) {
        return restTemplate
                .getForEntity(format("/submissions/pending?BIOSTDSESS=%s", sessionId), PendingSubmissionListDto.class);
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
