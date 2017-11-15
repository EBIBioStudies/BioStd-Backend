package uk.ac.ebi.biostd.exporter.jobs.full;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Contains application configuration properties extracted from configuration file application.yml.
 *
 * {@link #workers} reference the number of workers thread to process submissions. {@link #fileName} correspond to
 * generated output file name. {@link #filePath} refers to main submissions query modification i.e limit 100 to process
 * only 100 submissions.
 */
@Data
@Component
@ConfigurationProperties(prefix = "jobs.full")
public class FullExportJobProperties {

    private String fileName;
    private String filePath;
    private int workers;
    private String queryModified;
    private String notificationUrl;
}
