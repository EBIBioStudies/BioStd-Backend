package uk.ac.ebi.biostd.remote.dto;

import lombok.Data;

@Data
public class LoginResponseDto {

    private String status;
    private String sessid;
    private String username;
    private String email;
    private String superuser;
    private AuxInfoDto aux;
}
