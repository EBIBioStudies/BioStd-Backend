package uk.ac.ebi.biostd.webapp.application.security;

import lombok.Data;

@Data
public class RetryActivationRequest {

    private String email;
    private String activationURL;
}
