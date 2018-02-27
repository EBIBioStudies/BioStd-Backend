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
import javax.mail.MessagingException;
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
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.backend.configuration.TestConfiguration;
import uk.ac.ebi.biostd.webapp.application.security.entities.ChangePasswordRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.ResetPasswordRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignUpRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestConfiguration.class)
public class SecurityApiTest {

    private static final Pattern KEY_PATTERN = Pattern.compile("\"http:\\/\\/submission-tool\\/signup\\/(.*)\"");
    private static final Pattern KEY_PATTERN_2 = Pattern
            .compile("\"http:\\/\\/submission-tool\\/reset-password\\/(.*)\"");
    private static String NFS_PATH;

    @ClassRule
    public static TemporaryFolder TEST_FOLDER = new TemporaryFolder();

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeClass
    public static void beforeAll() throws IOException {
        NFS_PATH = TEST_FOLDER.getRoot().getPath();
        System.setProperty(BIOSTUDY_BASE_DIR, NFS_PATH);
        System.setProperty(CONFIG_FILE_LOCATION_VAR, NFS_PATH + "/config.properties");

        FileUtils.copyFile(
                new ClassPathResource("nfs/config.properties").getFile(),
                new File(NFS_PATH + "/config.properties"));
    }

    /**
     * Validates registration workflow.
     */
    @Test
    public void testRegistration() throws Exception {
        String code = signUp();
        activateUser(code);
        tryLogin("jhon_doe@ebi.ac.uk", "12345");
    }

    /**
     * Validates rest password workflow
     */
    @Test
    public void testResetPasswordRequest() {
        String user = "change_password@ebi.ac.uk";
        String activationKey = requestResetPassword(user);
        changePassword(activationKey, "newPassword");
        tryLogin(user, "newPassword");
    }

    @Test
    public void loginAndLogout() {
        String token = tryLogin("admin_user@ebi.ac.uk", "123456");
        logout(token);
    }

    private void logout(String token) {
        HttpEntity<String> response = restTemplate
                .exchange("/auth/signout?sessid=" + token, HttpMethod.POST, null,
                        String.class);
    }

    private void changePassword(String activationKey, String newPassword) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setPassword(newPassword);
        request.setKey(activationKey);

        restTemplate.postForObject("/auth/passreset", request, String.class);
    }

    private String requestResetPassword(String user) {
        ResetPasswordRequest passwordRequest = new ResetPasswordRequest();
        passwordRequest.setEmail(user);
        passwordRequest.setResetURL("http://submission-tool/reset-password/{KEY}");

        restTemplate.postForObject("/auth/passrstreq", passwordRequest, String.class);
        MimeMessage[] messages = greenMail.getReceivedMessages();
        Email email = EmailConverter.mimeMessageToEmail(messages[0]);

        return getActivationCode2(email.getPlainText());
    }

    public String signUp() throws MessagingException {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setEmail("jhon_doe@ebi.ac.uk");
        signUpRequest.setUsername("Juan Camilo Rada");
        signUpRequest.setPassword("12345");
        signUpRequest.setAux(Collections.singletonList("orcid:5657"));
        signUpRequest.setActivationURL("http://submission-tool/signup/{KEY}");

        restTemplate.postForObject("/auth/signup", signUpRequest, String.class);
        MimeMessage[] messages = greenMail.getReceivedMessages();

        Email email = EmailConverter.mimeMessageToEmail(messages[0]);

        return getActivationCode(email.getPlainText());
        //assertThat(email.getFromRecipient()).isEqualTo("biostudies@ebi.ac.uk");
    }

    public void activateUser(String activationKey) {
        restTemplate.postForObject("/auth/activate/" + activationKey, null, String.class);
    }

    public String tryLogin(String user, String password) {
        HttpEntity<String> response = restTemplate
                .exchange("/auth/signin?login=" + user + "&password=" + password, HttpMethod.POST, null,
                        String.class);
        HttpHeaders headers = response.getHeaders();
        String set_cookie = headers.getFirst(HttpHeaders.SET_COOKIE);
        assertThat(set_cookie).isNotEmpty();
        return set_cookie;
    }

    private String getActivationCode(String emailContent) {
        Matcher matcher = KEY_PATTERN.matcher(emailContent);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    private String getActivationCode2(String emailContent) {
        Matcher matcher = KEY_PATTERN_2.matcher(emailContent);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }
}
