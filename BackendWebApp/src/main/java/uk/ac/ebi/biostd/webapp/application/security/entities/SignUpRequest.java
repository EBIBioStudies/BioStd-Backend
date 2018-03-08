package uk.ac.ebi.biostd.webapp.application.security.entities;

import java.util.Collections;
import java.util.List;
import lombok.Data;

@Data
public class SignUpRequest {

    private String email;
    private String password;
    private String activationURL;
    private String username;
    private List<String> aux = Collections.emptyList();
}
