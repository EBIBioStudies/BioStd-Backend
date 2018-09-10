package uk.ac.ebi.biostd.webapp.application.rest.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.NoSuchFileException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserGroup;
import uk.ac.ebi.biostd.webapp.application.rest.dto.ServiceErrorDto;
import uk.ac.ebi.biostd.webapp.application.rest.exceptions.EntityNotFoundException;

public class ServiceExceptionHandlerTest {
    private static final String MESSAGE = "Test error message";

    private ServiceExceptionHandler testInstance;

    @Before
    public void setUp() {
        testInstance = new ServiceExceptionHandler();
    }

    @Test
    public void handleEntityNotFoundException() {
        EntityNotFoundException exception = new EntityNotFoundException(MESSAGE, UserGroup.class);
        ResponseEntity<ServiceErrorDto> response = testInstance.handleEntityNotFoundException(exception);

        assertResponse(response, UserGroup.class.getName(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void handleNoSuchFileException() {
        NoSuchFileException exception = new NoSuchFileException(MESSAGE);
        ResponseEntity<ServiceErrorDto> response = testInstance.handleNoSuchFileException(exception);

        assertResponse(response, NoSuchFileException.class.getName(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void handleUnsupportedOperationException() {
        UnsupportedOperationException exception = new UnsupportedOperationException(MESSAGE);
        ResponseEntity<ServiceErrorDto> response = testInstance.handleUnsupportedOperationException(exception);

        assertResponse(response, UnsupportedOperationException.class.getName(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    private void assertResponse(ResponseEntity<ServiceErrorDto> response, String entity, HttpStatus status) {
        ServiceErrorDto errorDto = response.getBody();

        assertThat(errorDto.getMessage()).isEqualTo(MESSAGE);
        assertThat(errorDto.getEntity()).isEqualTo(entity);
        assertThat(response.getStatusCode()).isEqualTo(status);
    }
}
