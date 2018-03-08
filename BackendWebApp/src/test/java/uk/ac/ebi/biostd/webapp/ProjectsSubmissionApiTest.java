package uk.ac.ebi.biostd.webapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.backend.configuration.TestConfiguration;
import uk.ac.ebi.biostd.backend.services.RemoteOperations;
import uk.ac.ebi.biostd.backend.testing.IntegrationTestUtil;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.ProjectsDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestConfiguration.class)
@DirtiesContext
@Sql(scripts = "/sql/projects.sql")
public class ProjectsSubmissionApiTest {

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
    public void testGetProjectsWhenAdmin() {
        String sessionId = operationsService.login("admin_user@ebi.ac.uk", "123456").getSessid();
        ResponseEntity<ProjectsDto> projectsDto = restTemplate
                .getForEntity("/atthost?BIOSTDSESS=" + sessionId, ProjectsDto.class);
        assertThat(projectsDto).isNotNull();
    }
}
