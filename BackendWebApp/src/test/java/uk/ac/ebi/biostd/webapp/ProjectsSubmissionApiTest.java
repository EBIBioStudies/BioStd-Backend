package uk.ac.ebi.biostd.webapp;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.biostd.webapp.application.configuration.ConfigProperties.CONFIG_FILE_LOCATION_VAR;
import static uk.ac.ebi.biostd.webapp.server.config.ConfigurationManager.BIOSTUDY_BASE_DIR;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.backend.configuration.TestConfiguration;
import uk.ac.ebi.biostd.backend.services.RemoteOperations;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.ProjectsDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestConfiguration.class)
@DirtiesContext
@Sql(scripts = "/sql/projects.sql")
public class ProjectsSubmissionApiTest {

    @ClassRule
    public static TemporaryFolder TEST_FOLDER = new TemporaryFolder();

    private static String NFS_PATH;

    @Autowired
    private TestRestTemplate restTemplate;

    private RemoteOperations operationsService;

    @BeforeClass
    public static void beforeAll() throws IOException {
        NFS_PATH = TEST_FOLDER.getRoot().getPath();
        System.setProperty(BIOSTUDY_BASE_DIR, NFS_PATH);
        System.setProperty(CONFIG_FILE_LOCATION_VAR, NFS_PATH + "/config.properties");

        FileUtils.copyFile(
                new ClassPathResource("nfs/config.properties").getFile(),
                new File(NFS_PATH + "/config.properties"));
    }

    @Before
    public void setup() {
        operationsService = new RemoteOperations(restTemplate);
    }

    @Test
    public void testGetProjectsWhenAdmin() {
        String sessionId = operationsService.login("admin_user@ebi.ac.uk", "123456").getSessid();
        ResponseEntity<ProjectsDto> projectsDto = restTemplate
                .getForEntity("/atthost?BIOSTDSESS=" + sessionId, ProjectsDto.class);
        assertThat(projectsDto).isNotNull();
    }
}
