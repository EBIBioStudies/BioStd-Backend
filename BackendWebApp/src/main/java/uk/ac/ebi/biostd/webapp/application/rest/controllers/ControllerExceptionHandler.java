package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.ac.ebi.biostd.webapp.application.rest.dto.ApiErrorDto;
import uk.ac.ebi.biostd.webapp.application.rest.exceptions.ApiErrorException;

@ControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(ApiErrorException.class)
    public ResponseEntity<ApiErrorDto> handleApiErrorException(ApiErrorException ex) {
        return new ResponseEntity<>(new ApiErrorDto(ex.getMessage()), ex.getStatus());
    }
}
