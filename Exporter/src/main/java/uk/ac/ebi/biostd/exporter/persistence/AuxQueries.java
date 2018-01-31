package uk.ac.ebi.biostd.exporter.persistence;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Declares the different queries used to obtain submissions information.
 */
@Data
@Component
@ConfigurationProperties
public class AuxQueries {

    private String submissionsTotalFileSize;
}
