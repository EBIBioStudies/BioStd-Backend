package uk.ac.ebi.biostd.exporter;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.PmcImportProperties;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.PmcImporter;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process.CvsTvsParser;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process.PmcFileManager;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process.SubmissionJsonSerializer;
import uk.ac.ebi.biostd.remote.service.RemoteService;
import uk.ac.ebi.biostd.util.FileUtil;

@Ignore
public class PmcImporterTest extends BaseIntegrationTest {

    private static final String USER_HOME_PATH =
            "/home/jcamilorada/Projects/BioStudies/NFS/ugindex/Users/j/jcamilorada@ebi.ac.uk.email";

    private final RestTemplate restTemplate = new RestTemplate();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private PmcImporter pmcImporter;
    private PmcImportProperties properties;

    @Before
    public void setup() throws IOException {

        String importPath = folder.getRoot().getAbsolutePath();
        FileUtil.copyDirectory(getResource("/pmc"), new File(importPath));

        properties = new PmcImportProperties();
        properties.setImportPath(importPath);
        properties.setSubmitterUserPath(USER_HOME_PATH);
        properties.setUser("jcamilorada@ebi.ac.uk");
        properties.setPassword("123456");
        properties.setBackendUrl("http://localhost:8586/biostd");

        pmcImporter = new PmcImporter(
                properties,
                new CvsTvsParser(),
                new PmcFileManager(properties.getSubmitterUserPath()),
                new RemoteService(properties.getBackendUrl()),
                new SubmissionJsonSerializer());
    }

    @Test
    public void execute() throws Exception {
        server.expect(once(), requestTo("/auth/signin"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{sessid: 12345}", MediaType.APPLICATION_JSON));

        pmcImporter.execute();
    }
}