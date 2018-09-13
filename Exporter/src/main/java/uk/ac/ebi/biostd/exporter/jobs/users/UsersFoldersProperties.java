package uk.ac.ebi.biostd.exporter.jobs.users;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Declared ftp file structure properties.
 *
 * {@link #symLinksPath} submissions folder path.
 * {@link #baseDropboxPath} main folder for users folder.
 */
@Data
@Component
@ConfigurationProperties(prefix = "jobs.users")
public class UsersFoldersProperties {

    private String baseDropboxPath;
    private String symLinksPath;
}
