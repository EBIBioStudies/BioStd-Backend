package uk.ac.ebi.biostd.webapp;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.biostd.webapp.application.security.rest.SecurityFilter.HEADER_NAME;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.MimeMessage;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.simplejavamail.converter.EmailConverter;
import org.simplejavamail.email.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;
import uk.ac.ebi.biostd.backend.configuration.TestConfiguration;
import uk.ac.ebi.biostd.backend.testing.IntegrationTestUtil;
import uk.ac.ebi.biostd.webapp.application.security.entities.ChangePasswordRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.ResetPasswordRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignInRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignUpRequest;
import uk.ac.ebi.biostd.webapp.application.security.error.ErrorMessage;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.SignoutRequestDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestConfiguration.class)
@DirtiesContext
public class SecurityApiTest {

    private static final String SIGN_OUT_URL = "/auth/signout?sessid=";
    private static final String PASS_REST_URL = "/auth/passreset";
    private static final String RESET_PASSWORD = "/auth/passrstreq";
    private static final String ACTIVATE_URL = "/auth/activate/";
    private static final String SIGN_URL = "/auth/signin";

    private static final Pattern SIGNUP_PATTERN = Pattern.compile("\"http://submission-tool/signup/(.*)\"");
    private static final Pattern RESET_PATTERN = Pattern.compile("\"http://submission-tool/reset-password/(.*)\"");

    @ClassRule
    public static TemporaryFolder TEST_FOLDER = new TemporaryFolder();

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeClass
    public static void beforeAll() throws IOException {
        IntegrationTestUtil.initFileSystem(TEST_FOLDER);
    }

    /* Validate simple login and logout */
    @Test
    public void loginAndLogout() {
        ResponseEntity<String> login = tryLogin("admin_user@ebi.ac.uk", "123456");
        Optional<HttpCookie> token = getCookie(login, HttpHeaders.SET_COOKIE);
        logout(token.get().getValue());
    }

    @Test
    public void loginWithQueryParams() {
        String url = format("%s?login=%s&password=%s", SIGN_URL, "admin_user@ebi.ac.uk", "123456");
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void validateLoginFail() {
        ResponseEntity<ErrorMessage> login = tryLogin("admin_user@ebi.ac.uk", "a_wrong_password", ErrorMessage.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    /*  Validates user registration workflow. */
    @Test
    public void testRegistration() throws Exception {
        String email = "jhon_doe@ebi.ac.uk";
        String password = "12345";
        String name = "Jhon Doe";

        String code = signUp(email, name, password);
        activateUser(code);
        tryLogin(email, password);
    }

    /* Validates security password reset workflow */
    @Test
    public void testResetPasswordRequest() {
        String user = "change_password@ebi.ac.uk";
        String activationKey = requestResetPassword(user);
        changePassword(activationKey, "newPassword");
        tryLogin(user, "newPassword");
    }

    @Test
    public void testCheckAccess() {
        String url = format("/checkAccess?login=%s&hash=%s",
                "admin_user@ebi.ac.uk",
                "7C4A8D09CA3762AF61E59520943DC26494F8941B");
        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
        assertThat(response.getBody()).isEqualTo("Status: OK\n"
                + "Allow: ~admin_user@ebi.ac.uk;#3;Public\n"
                + "Deny: \n"
                + "Superuser: true\n"
                + "Name: admin_user\n"
                + "Login: admin_user@ebi.ac.uk\n"
                + "EMail: admin_user@ebi.ac.uk\n");
    }

    private void logout(String token) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(HEADER_NAME, token);
        HttpEntity<SignoutRequestDto> request = new HttpEntity<>(new SignoutRequestDto(token), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(SIGN_OUT_URL + token, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String cookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(cookie).isEqualTo("BIOSTDSESS=; Max-Age=0; Expires=Thu, 01-Jan-1970 00:00:10 GMT");
    }

    private void changePassword(String activationKey, String newPassword) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setPassword(newPassword);
        request.setKey(activationKey);

        restTemplate.postForObject(PASS_REST_URL, request, String.class);
    }

    private String requestResetPassword(String user) {
        ResetPasswordRequest passwordRequest = new ResetPasswordRequest();
        passwordRequest.setEmail(user);
        passwordRequest.setResetURL("http://submission-tool/reset-password/{KEY}");

        restTemplate.postForObject(RESET_PASSWORD, passwordRequest, String.class);
        MimeMessage[] messages = greenMail.getReceivedMessages();
        Email email = EmailConverter.mimeMessageToEmail(messages[0]);

        return extractKey(email.getHTMLText(), RESET_PATTERN);
    }

    private String signUp(String email, String name, String password) {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setEmail(email);
        signUpRequest.setUsername(name);
        signUpRequest.setPassword(password);
        signUpRequest.setAux(Collections.singletonList("orcid:5657"));
        signUpRequest.setActivationURL("http://submission-tool/signup/{KEY}");

        restTemplate.postForObject("/auth/signup", signUpRequest, String.class);
        MimeMessage[] messages = greenMail.getReceivedMessages();

        assertThat(messages).hasSize(1);
        Email notification = EmailConverter.mimeMessageToEmail(messages[0]);
        return extractKey(notification.getHTMLText(), SIGNUP_PATTERN);

    }

    private void activateUser(String activationKey) {
        ResponseEntity<String> response = restTemplate.postForEntity(ACTIVATE_URL + activationKey, null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    private ResponseEntity<String> tryLogin(String user, String password) {
        return tryLogin(user, password, String.class);
    }

    private <T> ResponseEntity<T> tryLogin(String user, String password, Class<T> responseType) {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setLogin(user);
        signInRequest.setPassword(password);
        return restTemplate.postForEntity(SIGN_URL, signInRequest, responseType);
    }

    private String extractKey(String emailContent, Pattern pattern) {
        Matcher matcher = pattern.matcher(emailContent);
        assertThat(matcher.find()).isTrue().as("can not find expected regex");
        return matcher.group(1);
    }

    private Optional<HttpCookie> getCookie(HttpEntity<?> entity, String name) {
        HttpHeaders headers = entity.getHeaders();
        return HttpCookie.parse(headers.getFirst(name)).stream().findFirst();
    }
}
