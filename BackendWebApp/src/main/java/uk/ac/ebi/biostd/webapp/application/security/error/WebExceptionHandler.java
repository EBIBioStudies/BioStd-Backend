package uk.ac.ebi.biostd.webapp.application.security.error;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Component
public class WebExceptionHandler {

    private static final String FAIL_STATUS = "FAIL";

    @ExceptionHandler(value = SecurityAccessException.class)
    public ErrorMessage handleConflict(SecurityAccessException exception) {
        return new ErrorMessage(exception.getMessage(), FAIL_STATUS);
    }
}
