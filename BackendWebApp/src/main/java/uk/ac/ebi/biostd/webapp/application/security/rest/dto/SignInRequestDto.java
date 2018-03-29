package uk.ac.ebi.biostd.webapp.application.security.rest.dto;

import lombok.Data;

@Data
public class SignInRequestDto {

    private String login;
    private String password;
}
