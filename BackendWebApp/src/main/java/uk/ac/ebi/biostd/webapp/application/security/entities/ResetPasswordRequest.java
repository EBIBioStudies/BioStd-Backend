package uk.ac.ebi.biostd.webapp.application.security.entities;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String password;
    private String key;
    private String SuccessURL = "successURL";
    private String failURL = "failURL";
}
