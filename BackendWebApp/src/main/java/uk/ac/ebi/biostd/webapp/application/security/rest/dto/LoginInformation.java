package uk.ac.ebi.biostd.webapp.application.security.rest.dto;

import lombok.Data;

@Data
public class LoginInformation {

    private String login;
    private String hash;
}
