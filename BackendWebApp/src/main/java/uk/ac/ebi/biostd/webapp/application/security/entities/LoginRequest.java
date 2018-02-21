package uk.ac.ebi.biostd.webapp.application.security.entities;

import lombok.Data;

@Data
public class LoginRequest {

    private String login;
    private String hash;
}
