package uk.ac.ebi.biostd.webapp.application.security.rest.dto;

import lombok.Data;

@Data
public class LoginResponseDto {

    private String status;
    private String sessid;
    private String username;
    private String email;
    private String superuser;
    private String dropbox;
    private AuxInfoDto aux;
}
