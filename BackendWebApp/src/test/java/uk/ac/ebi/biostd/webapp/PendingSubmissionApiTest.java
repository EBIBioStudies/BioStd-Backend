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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.backend.configuration.TestConfiguration;
import uk.ac.ebi.biostd.backend.services.RemoteOperations;
import uk.ac.ebi.biostd.backend.testing.IntegrationTestUtil;
import uk.ac.ebi.biostd.backend.testing.ResourceHandler;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitReportDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitStatus;

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
        getAllPendingSubmissions(sessionId).getSubmissions()
                .forEach(subm -> deletePendingSubmission(subm.getAccno(), sessionId));
    }

    @Test
    public void testCreatePendingSubmission() {
        String sessionId = login();

        String data = getSubmissionSample();

        PendingSubmissionDto dto = createPendingSubmission(data, sessionId);

        assertThat(dto).isNotNull();
        assertThat(dto.getAccno()).matches("TMP_.+");
        assertThat(dto.getChanged()).isGreaterThan(0);
        assertThat(dto.getData().toString()).isEqualTo(data);

        PendingSubmissionDto dtoCopy = getPendingSubmission(dto.getAccno(), sessionId);

        assertThat(dtoCopy).isNotNull();
        assertThat(dtoCopy.getAccno()).isEqualTo(dto.getAccno());
        assertThat(dtoCopy.getChanged()).isEqualTo(dto.getChanged());
        assertThat(dtoCopy.getData().toString()).isEqualTo(dto.getData().toString());
    }


    @Test
    public void testUpdatePendingSubmission() {
        String sessionId = login();
        String data = getSubmissionSample();

        PendingSubmissionDto dto1 = createPendingSubmission(data, sessionId);
        long mTime1 = dto1.getChanged();

        updatePendingSubmission(dto1.getAccno(), data, sessionId);
        PendingSubmissionDto dto2 = getPendingSubmission(dto1.getAccno(), sessionId);
        long mTime2 = dto2.getChanged();

        assertThat(mTime1).isLessThan(mTime2);
    }

    @Test
    public void testGetPendingSubmissionList() {
        PendingSubmissionListDto dto = restTemplate
                .getForEntity(format("/submissions/pending?BIOSTDSESS=%s", login()), PendingSubmissionListDto.class)
                .getBody();

        assertThat(dto).isNotNull();
        assertThat(dto.getSubmissions()).isNotNull();
        assertThat(dto.getSubmissions()).isEmpty();
    }

    @Test
    public void testSubmitPendingSubmission() {
        String sessionId = login();
        PendingSubmissionDto dto = createPendingSubmission(getSubmissionSample(), sessionId);

        SubmitReportDto report = submitPendingSubmission(dto.getAccno(), sessionId);

        assertThat(report.getStatus()).isEqualTo(SubmitStatus.OK);
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

    private PendingSubmissionDto createPendingSubmission(String data, String sessionId) {
        return restTemplate
                .postForEntity(format("/submissions/pending?BIOSTDSESS=%s", sessionId),
                        new HttpEntity<>(data, headers()), PendingSubmissionDto.class).getBody();
    }

    private void updatePendingSubmission(String accno, String data, String sessionId) {
        restTemplate
                .put(format("/submissions/pending/%s?BIOSTDSESS=%s", accno, sessionId),
                        new HttpEntity<>(data, headers()));
    }

    private SubmitReportDto submitPendingSubmission(String accno, String sessionId) {
        return restTemplate
                .postForEntity(format("/submissions/pending/%s/submit?BIOSTDSESS=%s", accno, sessionId),
                        new HttpEntity<>("{}", headers()), SubmitReportDto.class).getBody();
    }

    private PendingSubmissionDto getPendingSubmission(String accno, String sessionId) {
        return restTemplate
                .getForEntity(format("/submissions/pending/%s?BIOSTDSESS=%s", accno, sessionId),
                        PendingSubmissionDto.class).getBody();
    }

    private PendingSubmissionListDto getAllPendingSubmissions(String sessionId) {
        return restTemplate
                .getForEntity(format("/submissions/pending?BIOSTDSESS=%s", sessionId),
                        PendingSubmissionListDto.class).getBody();
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
