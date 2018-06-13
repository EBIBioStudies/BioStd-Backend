package uk.ac.ebi.biostd.webapp;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.*;
import org.junit.rules.ExpectedException;
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
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitReportDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestConfiguration.class)
@DirtiesContext
public class PendingSubmissionApiTest {

    private static final String NEW_PAGETAB = "input/EMPTY.pagetab.json";

    @ClassRule
    public static TemporaryFolder TEST_FOLDER = new TemporaryFolder();

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

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
    public void testCreatePendingSubmissionBadRequest() {
        expectedEx.expect(HttpResponseStatusException.class);
        expectedEx.expect(new HttpResponseStatusExceptionMatcher(HttpStatus.BAD_REQUEST));

        createPendingSubmission("not a json data", login());
    }

    @Test()
    public void testGetNoneExistedSubmission() {
        expectedEx.expect(HttpResponseStatusException.class);
        expectedEx.expect(new HttpResponseStatusExceptionMatcher(HttpStatus.BAD_REQUEST));

        getPendingSubmission("12345", login());
    }

    @Test
    public void testUpdatePendingSubmission() {
        String sessionId = login();
        String data = getSubmissionSample();

        PendingSubmissionDto dto1 = createPendingSubmission(data, sessionId);
        long mTime1 = dto1.getChanged();

        PendingSubmissionDto dto2 = updatePendingSubmission(dto1.getAccno(), data, sessionId);
        long mTime2 = dto2.getChanged();

        assertThat(mTime1).isLessThan(mTime2);
    }

    @Test
    public void testUpdatePendingSubmissionBadRequest() {
        expectedEx.expect(HttpResponseStatusException.class);
        expectedEx.expect(new HttpResponseStatusExceptionMatcher(HttpStatus.BAD_REQUEST));

        updatePendingSubmission("111", "not a json data", login());
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
        PendingSubmissionDto dto = createPendingSubmission(getSubmissionSample(), sessionId);

        SubmitReportDto report = submitPendingSubmission(dto.getAccno(), sessionId);

        assertThat(report.getStatus()).isEqualTo("OK");

        expectedEx.expect(HttpResponseStatusException.class);
        expectedEx.expect(new HttpResponseStatusExceptionMatcher(HttpStatus.BAD_REQUEST));

        getPendingSubmission(dto.getAccno(), sessionId);
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
        return getBody(restTemplate
                .postForEntity(format("/submissions/pending?BIOSTDSESS=%s", sessionId),
                        new HttpEntity<>(data, headers()), PendingSubmissionDto.class));
    }

    private PendingSubmissionDto updatePendingSubmission(String accno, String data, String sessionId) {
        return getBody(restTemplate
                .postForEntity(format("/submissions/pending/%s?BIOSTDSESS=%s", accno, sessionId),
                        new HttpEntity<>(data, headers()), PendingSubmissionDto.class));
    }

    private SubmitReportDto submitPendingSubmission(String accno, String sessionId) {
        return getBody(restTemplate
                .postForEntity(format("/submissions/pending/%s/submit?BIOSTDSESS=%s", accno, sessionId),
                        HttpEntity.EMPTY, SubmitReportDto.class));
    }

    private PendingSubmissionDto getPendingSubmission(String accno, String sessionId) {
        return getBody(restTemplate
                .getForEntity(format("/submissions/pending/%s?BIOSTDSESS=%s", accno, sessionId),
                        PendingSubmissionDto.class));
    }

    private PendingSubmissionListDto getAllPendingSubmissions(String sessionId) {
        return getBody(restTemplate
                .getForEntity(format("/submissions/pending?BIOSTDSESS=%s", sessionId),
                        PendingSubmissionListDto.class));
    }

    private <T> T getBody(ResponseEntity<T> responseEntity) {
        if (responseEntity.getStatusCode().value() >= HttpStatus.BAD_REQUEST.value()) {
            throw new HttpResponseStatusException(responseEntity.getStatusCode());
        }
        return responseEntity.getBody();
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private static class HttpResponseStatusException extends RuntimeException {
        private final HttpStatus status;

        HttpResponseStatusException(HttpStatus status) {
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }

    private static class HttpResponseStatusExceptionMatcher extends TypeSafeMatcher<HttpResponseStatusException> {
        private final HttpStatus httpStatus;

        HttpResponseStatusExceptionMatcher(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
        }

        @Override
        protected boolean matchesSafely(HttpResponseStatusException ex) {
            return ex.getStatus().equals(httpStatus);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("expects code ")
                    .appendValue(httpStatus);
        }
    }
}
