package uk.ac.ebi.biostd.exporter.jobs.partial;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PartialExporterCleanerTest {
    private String tempFolderRoot;

    private PartialExportCleaner testInstance;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        tempFolderRoot = tempFolder.getRoot().getAbsolutePath();
        setUpTestFiles();

        testInstance = new PartialExportCleaner(createTestProperties());
    }

    @Test
    public void execute() {
        testInstance.execute();

        assertFalse(Files.exists(Paths.get(tempFolderRoot + "studies_partial_2020_04_05_07_35.json")));
        assertFalse(Files.exists(Paths.get(tempFolderRoot + "studies_partial_2020_04_05_13_25.json")));
    }

    private void setUpTestFiles() throws IOException {
        tempFolder.newFile("studies_partial_2020_04_05_07_35.json");
        tempFolder.newFile("studies_partial_2020_04_05_13_25.json");
    }

    private PartialExportJobProperties createTestProperties() {
        PartialExportJobProperties testConfig = new PartialExportJobProperties();
        testConfig.setFileName("studies_partial");
        testConfig.setFilePath(tempFolderRoot);

        return testConfig;
    }
}
