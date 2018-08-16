package uk.ac.ebi.biostd.webapp.application.rest.service;

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
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ServiceErrorDto(exception.getMessage(), exception.getEntity().getName()));
    }
}
