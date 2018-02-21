package uk.ac.ebi.biostd.webapp.application.security.entities;

import lombok.Data;

@Data
public class SignUpRequest {

    private String aux;
    private String email;
    private String password;
    private String activationURL;
    private String username;
}
