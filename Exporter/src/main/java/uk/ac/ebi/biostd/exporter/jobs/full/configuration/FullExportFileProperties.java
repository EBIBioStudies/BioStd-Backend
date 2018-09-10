package uk.ac.ebi.biostd.exporter.jobs.full.configuration;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class FullExportFileProperties {
    private String fileName;
    private String filePath;
}
