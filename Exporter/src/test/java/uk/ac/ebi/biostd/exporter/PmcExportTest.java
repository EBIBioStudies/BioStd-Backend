package uk.ac.ebi.biostd.exporter;

import static com.jamesmurty.utils.XMLBuilder.create;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import com.google.common.collect.ImmutableList;
import com.jamesmurty.utils.XMLBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.TestConfiguration;
import uk.ac.ebi.biostd.exporter.jobs.common.api.ExportPipeline;
import uk.ac.ebi.biostd.exporter.jobs.pmc.export.PmcExport;
import uk.ac.ebi.biostd.exporter.jobs.pmc.export.PmcJobsFactory;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
@Sql(scripts = {"classpath:create_schema.sql", "classpath:init-pmc.sql"}, executionPhase = BEFORE_TEST_METHOD)
public class PmcExportTest {

    private static final String FTP_PATH = "/data";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Autowired
    private PmcExport pmcExport;

    @Autowired
    private PmcJobsFactory pmcJobsFactory;

    private FakeFtpServer fakeFtpServer;

    private ExportPipeline exportPipeline;

    @Before
    public void before() {
        exportPipeline = new ExportPipeline(1, ImmutableList.of(pmcExport), pmcJobsFactory);

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry(FTP_PATH));

        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.addUserAccount(new UserAccount("pmc", "pmc", FTP_PATH));
        fakeFtpServer.setServerControlPort(43120);
        fakeFtpServer.start();
    }

    @After
    public void after() {
        fakeFtpServer.stop();
    }

    @Test
    public void testPmcExport() throws Exception {
        exportPipeline.execute();

        List<FileEntry> files = fakeFtpServer.getFileSystem().listFiles(FTP_PATH);
        assertThat(files).hasSize(1);
        assertThat(getContent(files.get(0))).isXmlEqualTo(getExpectedXml());

    }

    private String getContent(FileEntry fileEntry) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(fileEntry.createInputStream(), writer, UTF_8);
        return writer.toString();
    }

    private String getExpectedXml() throws ParserConfigurationException, TransformerException {
        // @formatter:off
        XMLBuilder builder =
                create("links")
                    .element("link").attribute("providerId", "1518").
                        element("resource").
                            element("url").text("http://www.ebi.ac.uk/biostudies/studies/S-EPMC4273986?xr=true").up().
                            element("title").text("Biology by rational protein-RNA engineering.").up(2).
                        element("record").
                            element("source").text("PMC").up().
                            element("id").text("PMC4273986");
        // @formatter:on
        return builder.asString();
    }

}