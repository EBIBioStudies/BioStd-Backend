package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.ac.ebi.biostd.webapp.application.rest.dto.ApiErrorDto;
import uk.ac.ebi.biostd.webapp.application.rest.exceptions.ApiErrorException;

public class ControllerExceptionHandlerTest {
    private static final String MESSAGE = "Test error message";

    private ControllerExceptionHandler testInstance;

    @Before
    public void setUp() {
        testInstance = new ControllerExceptionHandler();
    }

    @Test
    public void handleApiErrorException() {
        ApiErrorException exception = new ApiErrorException(MESSAGE, HttpStatus.NOT_FOUND);
        ResponseEntity<ApiErrorDto> response = testInstance.handleApiErrorException(exception);

        assertThat(response.getBody().getMessage()).isEqualTo(MESSAGE);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
