package uk.ac.ebi.biostd.webapp.application.security.entities;

import lombok.Data;

@Data
public class ActivateRequest {

    private String successURL;
    private String failURL;
}
