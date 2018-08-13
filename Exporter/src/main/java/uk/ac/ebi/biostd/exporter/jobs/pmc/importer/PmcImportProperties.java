package uk.ac.ebi.biostd.exporter.jobs.pmc.importer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jobs.pmc.import")
public class PmcImportProperties {

    private String importPath;
    private String submitterUserPath;
    private String user;
    private String password;
}
