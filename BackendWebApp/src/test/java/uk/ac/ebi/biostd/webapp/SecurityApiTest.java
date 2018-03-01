package uk.ac.ebi.biostd.webapp;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.biostd.webapp.application.configuration.ConfigProperties.CONFIG_FILE_LOCATION_VAR;
import static uk.ac.ebi.biostd.webapp.server.config.ConfigurationManager.BIOSTUDY_BASE_DIR;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.MimeMessage;
import org.apache.commons.io.FileUtils;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.backend.configuration.TestConfiguration;
import uk.ac.ebi.biostd.webapp.application.security.entities.ChangePasswordRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.ResetPasswordRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignInRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignUpRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestConfiguration.class)
public class SecurityApiTest {

    private static final String SIGN_OUT_URL = "/auth/signout?sessid=";
    private static final String PASS_REST = "/auth/passreset";
    private static final String RESET_PASSWORD = "/auth/passrstreq";
    private static final String acticate = "/auth/activate/";

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
        String NFS_PATH = TEST_FOLDER.getRoot().getPath();
        System.setProperty(BIOSTUDY_BASE_DIR, NFS_PATH);
        System.setProperty(CONFIG_FILE_LOCATION_VAR, NFS_PATH + "/config.properties");

        FileUtils.copyFile(
                new ClassPathResource("nfs/config.properties").getFile(),
                new File(NFS_PATH + "/config.properties"));
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

    /* Validate simple login and logout */
    @Test
    public void loginAndLogout() {
        String token = tryLogin("admin_user@ebi.ac.uk", "123456");
        logout(token);
    }

    private void logout(String token) {
        ResponseEntity<String> response = restTemplate.postForEntity(SIGN_OUT_URL + token, null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String cookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(cookie).isEqualTo("BIOSTDSESS=\"\"; Expires=Thu, 01-Jan-1970 00:00:10 GMT");
    }

    private void changePassword(String activationKey, String newPassword) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setPassword(newPassword);
        request.setKey(activationKey);

        restTemplate.postForObject(PASS_REST, request, String.class);
    }

    private String requestResetPassword(String user) {
        ResetPasswordRequest passwordRequest = new ResetPasswordRequest();
        passwordRequest.setEmail(user);
        passwordRequest.setResetURL("http://submission-tool/reset-password/{KEY}");

        restTemplate.postForObject(RESET_PASSWORD, passwordRequest, String.class);
        MimeMessage[] messages = greenMail.getReceivedMessages();
        Email email = EmailConverter.mimeMessageToEmail(messages[0]);

        return extractKey(email.getPlainText(), RESET_PATTERN);
    }

    public String signUp(String email, String name, String password) {
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
        return extractKey(notification.getPlainText(), SIGNUP_PATTERN);

    }

    public void activateUser(String activationKey) {
        ResponseEntity<String> response = restTemplate.postForEntity(acticate + activationKey, null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    public String tryLogin(String user, String password) {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setLogin(user);
        signInRequest.setPassword(password);
        HttpEntity<String> response = restTemplate.postForEntity("/auth/signin", signInRequest, String.class);
        HttpHeaders headers = response.getHeaders();
        String set_cookie = headers.getFirst(HttpHeaders.SET_COOKIE);
        assertThat(set_cookie).isNotEmpty();
        return set_cookie;
    }

    private String extractKey(String emailContent, Pattern pattern) {
        Matcher matcher = pattern.matcher(emailContent);
        assertThat(matcher.find()).isTrue().as("can not find expected regex");
        return matcher.group(1);
    }
}
