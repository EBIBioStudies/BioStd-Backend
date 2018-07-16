package uk.ac.ebi.biostd.webapp.application.rest.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ApiErrorException extends RuntimeException {

    @Getter
    private final HttpStatus status;

    public ApiErrorException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
