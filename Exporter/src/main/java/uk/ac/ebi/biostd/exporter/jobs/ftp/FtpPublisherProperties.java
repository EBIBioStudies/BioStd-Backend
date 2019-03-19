package uk.ac.ebi.biostd.exporter.jobs.ftp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Declared ftp file structure properties.
 *
 * {@link #baseBioStudiesPath} submissions folder path.
 * {@link #baseFtpPath} main ftp shared folder.
 */
@Data
@Component
@ConfigurationProperties(prefix = "jobs.ftp")
public class FtpPublisherProperties {
    private String baseFtpPath;
    private String baseBioStudiesPath;
}
