package uk.ac.ebi.biostd.exporter;


import java.io.File;
import java.net.URL;

class BaseIntegrationTest {

    protected File getResource(String path) {
        URL url = this.getClass().getResource(path);
        return new File(url.getFile());
    }
}
