package uk.ac.ebi.biostd.webapp.application.security.entities;

import lombok.Data;

@Data
public class ChangePasswordRequest {

    private String password;
    private String key;
}
