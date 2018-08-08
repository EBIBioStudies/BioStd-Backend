package uk.ac.ebi.biostd.exporter;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.twice;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONObject;
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

        pmcImporter = new PmcImporter(
                properties,
                new CvsTvsParser(),
                new PmcFileManager(properties),
                new RemoteService(restTemplate),
                new SubmissionJsonSerializer());
    }

    @Test
    public void execute() throws Exception {
        server.expect(once(), requestTo("/auth/signin"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(securityLoginResponseJson(), MediaType.APPLICATION_JSON));

        server.expect(twice(), requestTo("/submit/createupdate?BIOSTDSESS=12345"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(submissionResponse(), MediaType.APPLICATION_JSON));

        pmcImporter.execute();
    }

    private String securityLoginResponseJson() {
        return new JSONObject().put("sessid", 12345).toString();
    }

    private String submissionResponse() {
        return new JSONObject()
                .put("status", "OK")
                .put("mapping", newArray(
                        newObject(new SimpleEntry<>("order", "1")),
                        newObject(new SimpleEntry<>("assigned", "")),
                        newObject(new SimpleEntry<>("original", ""))))
                .put("log", new JSONObject()
                        .put("message", "CREATEUPDATE submission(s) from json source")
                        .put("level", "SUCCESS")
                        .put("subnodes", newArray(
                                newNode("Processing 'json' data. Body size: 9314", "INFO"),
                                newNode("Parsing JSON body", "SUCCESS"),
                                newNode("Database transaction successful", "INFO")))).toString();
    }

    private JSONArray newArray(JSONObject... objects) {
        JSONArray jsonArray = new JSONArray();
        for (JSONObject jsonObject : objects) {
            jsonArray.put(jsonObject);
        }

        return jsonArray;
    }

    private JSONObject newNode(String message, String level) {
        return new JSONObject()
                .put("message", message)
                .put("INFO", level);
    }

    @SafeVarargs
    private final JSONObject newObject(SimpleEntry<String, String>... entries) {
        JSONObject jsonObject = new JSONObject();
        for (Entry<String, String> entry : entries) {
            jsonObject.put(entry.getKey(), entry.getValue());
        }
        return jsonObject;
    }
}