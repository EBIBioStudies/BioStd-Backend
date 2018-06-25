package uk.ac.ebi.biostd.exporter.jobs.partial;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jobs.partial")
public class PartialExportJobProperties {

    private String fileName;
    private String filePath;
    private String notificationUrl;
    private String deleteNotificationUrl;
}
