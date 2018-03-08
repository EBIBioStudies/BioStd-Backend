package uk.ac.ebi.biostd.webapp.application.security.error;

public class SecurityAccessException extends RuntimeException {

    public SecurityAccessException(String message) {
        super(message);
    }
}
