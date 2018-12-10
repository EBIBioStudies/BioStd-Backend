package uk.ac.ebi.biostd.exporter.configuration;

import java.util.List;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Component
@ConfigurationProperties(prefix = "jobs")
public class ExporterGeneralProperties {
    private List<String> libFileStudies;

    public List<String> getLibFileStudies() {
        return libFileStudies.isEmpty() ? null : libFileStudies;
    }
}
