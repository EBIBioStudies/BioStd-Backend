package uk.ac.ebi.biostd.webapp.application.security.entities;

import lombok.Data;

@Data
public class SignInRequest {

    private String login;
    private String password;
}
