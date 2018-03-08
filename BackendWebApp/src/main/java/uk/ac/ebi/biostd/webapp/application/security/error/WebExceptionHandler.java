package uk.ac.ebi.biostd.webapp.application.security.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Component
@ControllerAdvice
public class WebExceptionHandler {

    private static final String FAIL_STATUS = "FAIL";

    @ExceptionHandler(value = SecurityAccessException.class)
    public ResponseEntity<ErrorMessage> handleConflict(SecurityAccessException exception) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorMessage(exception.getMessage(), FAIL_STATUS));
    }
}
