package uk.ac.ebi.biostd.exporter.persistence.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDropboxInfo {

    private long id;
    private String secret;
    private String email;
}
