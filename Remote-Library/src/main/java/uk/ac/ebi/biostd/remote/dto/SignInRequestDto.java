package uk.ac.ebi.biostd.remote.dto;

import lombok.Data;

@Data
public class SignInRequestDto {

    private String login;
    private String password;
}
