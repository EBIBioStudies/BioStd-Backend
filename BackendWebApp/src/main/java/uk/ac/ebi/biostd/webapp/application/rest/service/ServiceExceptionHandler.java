package uk.ac.ebi.biostd.webapp.application.rest.service;

import java.nio.file.NoSuchFileException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.biostd.webapp.application.rest.dto.ServiceErrorDto;
import uk.ac.ebi.biostd.webapp.application.rest.exceptions.EntityNotFoundException;

@ControllerAdvice
public class ServiceExceptionHandler {
    @ResponseBody
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ServiceErrorDto> handleEntityNotFoundException(EntityNotFoundException exception) {
        return createResponse(HttpStatus.NOT_FOUND, exception.getMessage(), exception.getEntity().getName());
    }

    @ResponseBody
    @ExceptionHandler(NoSuchFileException.class)
    public ResponseEntity<ServiceErrorDto> handleNoSuchFileException(NoSuchFileException exception) {
        return createResponse(HttpStatus.NOT_FOUND, exception.getMessage(), exception.getClass().getName());
    }

    @ResponseBody
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ServiceErrorDto> handleUnsupportedOperationException(UnsupportedOperationException exception) {
        return createResponse(HttpStatus.METHOD_NOT_ALLOWED, exception.getMessage(), exception.getClass().getName());
    }

    private ResponseEntity<ServiceErrorDto> createResponse(HttpStatus status, String message, String entity) {
        return ResponseEntity.status(status).body(new ServiceErrorDto(message, entity));
    }
}
