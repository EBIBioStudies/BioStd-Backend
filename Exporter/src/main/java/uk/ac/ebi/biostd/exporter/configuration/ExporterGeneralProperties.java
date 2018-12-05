package uk.ac.ebi.biostd.exporter.configuration;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jobs")
public class ExporterGeneralProperties {
    private List<String> libFileStudies;
}
