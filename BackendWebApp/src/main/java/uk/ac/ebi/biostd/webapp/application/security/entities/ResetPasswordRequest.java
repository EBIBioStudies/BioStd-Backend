package uk.ac.ebi.biostd.webapp.application.security.entities;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String email;
    private String resetURL;

}
