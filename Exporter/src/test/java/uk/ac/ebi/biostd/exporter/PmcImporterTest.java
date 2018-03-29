package uk.ac.ebi.biostd.exporter;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.PmcImportProperties;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.PmcImporter;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process.CvsTvsParser;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.process.PmcSubmissionManager;
import uk.ac.ebi.biostd.remote.service.RemoteService;


public class PmcImporterTest {

    private PmcImporter pmcImporter;
    private PmcImportProperties properties;

    @Before
    public void setup() {
        properties = new PmcImportProperties();
        properties.setImportPath(
                "/home/jcamilorada/Projects/BioStudies/BioStd-Backend/Exporter/src/test/resources/pmc");
        properties.setSubmitterUserPath(
                "/home/jcamilorada/Projects/BioStudies/NFS/ugindex/Users/j/jcamilorada@ebi.ac.uk.email");
        properties.setUser("jcamilorada@ebi.ac.uk");
        properties.setPassword("123456");

        pmcImporter = new PmcImporter(properties,
                new CvsTvsParser(),
                new PmcSubmissionManager(properties.getSubmitterUserPath()),
                new RemoteService("http://localhost:8586/biostd"));
    }

    @Test
    public void execute() throws Exception {
        pmcImporter.execute();
    }
}