package uk.ac.ebi.biostd.exporter;


import java.io.File;
import java.net.URL;

class BaseIntegrationTest {

    File getResource(String path) {
        URL url = this.getClass().getResource(path);
        return new File(url.getFile());
    }
}
