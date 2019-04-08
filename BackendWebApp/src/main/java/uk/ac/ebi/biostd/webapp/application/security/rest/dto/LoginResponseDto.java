package uk.ac.ebi.biostd.webapp.application.security.rest.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponseDto {

    private String status;
    private String sessid;
    private String username;
    private String fullname;
    private String email;
    private String superuser;
    private String secret;
    private List<String> allow;
    private List<String> deny;
    private AuxInfoDto aux;

}
