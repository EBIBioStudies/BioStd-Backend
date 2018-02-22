package uk.ac.ebi.biostd.webapp;

import static uk.ac.ebi.biostd.webapp.application.configuration.ConfigProperties.CONFIG_FILE_LOCATION_VAR;
import static uk.ac.ebi.biostd.webapp.server.config.ConfigurationManager.BIOSTUDY_BASE_DIR;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.io.File;
import java.io.IOException;
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
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.backend.configuration.TestConfiguration;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignUpRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestConfiguration.class)
public class SecurityApiTest {

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

    @Test
    public void signUp() throws MessagingException {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setEmail("jhon_doe@ebi.ac.uk");
        signUpRequest.setUsername("Juan Camilo Rada");
        signUpRequest.setPassword("12345");
        signUpRequest.setActivationURL("http://submission-tool/signup");

        restTemplate.postForObject("/auth/signup", signUpRequest, String.class);
        MimeMessage[] messages = greenMail.getReceivedMessages();

        Email email = EmailConverter.mimeMessageToEmail(messages[0]);

        /*
        MimeMessage message = messages[0];
        String body = GreenMailUtil.getBody(message);
        String to = GreenMailUtil.getAddressList(message.getAllRecipients());
        String from = GreenMailUtil.*/
    }
}
