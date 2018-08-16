package uk.ac.ebi.biostd.webapp.application.rest.service;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void handleEntityNotFoundExceptio() {
        EntityNotFoundException exception = new EntityNotFoundException(MESSAGE, UserGroup.class);
        ResponseEntity<ServiceErrorDto> response = testInstance.handleEntityNotFoundException(exception);
        ServiceErrorDto errorDto = response.getBody();

        assertThat(errorDto.getMessage()).isEqualTo(MESSAGE);
        assertThat(errorDto.getEntity()).isEqualTo(UserGroup.class.getName());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
