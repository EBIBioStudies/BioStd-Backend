package uk.ac.ebi.biostd.webapp.application.common.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ControllerErrorHandler {

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleSecurityException(SecurityException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> exception() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("FAIL User not logged in");
    }
}
