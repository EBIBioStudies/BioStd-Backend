package uk.ac.ebi.biostd.webapp.application.security.entities;

import java.util.Collections;
import java.util.List;
import lombok.Data;

@Data
public class SignUpRequest {

    private String email;
    private String password;
    private String username;
    private List<String> aux = Collections.emptyList();

    private String instanceKey;
    private String path;
}
