package uk.ac.ebi.biostd.webapp.application.security.entities;

import lombok.Data;

@Data
public class RetryActivationRequest {

    private String email;

    private String instanceKey;
    private String path;
}
