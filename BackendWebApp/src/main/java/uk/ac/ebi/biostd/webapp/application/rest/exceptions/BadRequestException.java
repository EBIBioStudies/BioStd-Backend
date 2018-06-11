package uk.ac.ebi.biostd.webapp.application.rest.exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
