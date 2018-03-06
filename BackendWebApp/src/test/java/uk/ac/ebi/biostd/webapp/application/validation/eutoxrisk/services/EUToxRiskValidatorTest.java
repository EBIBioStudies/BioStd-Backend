package uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biostd.backend.configuration.TestConfiguration;
import uk.ac.ebi.biostd.backend.testing.ResourceHandler;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.dto.EUToxRiskFileValidationError;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Olga Melnichuk
 */
@RunWith(SpringRunner.class)
@Import(TestConfiguration.class)
public class EUToxRiskValidatorTest {

    @Autowired
    @Qualifier("eutoxrisk")
    private RestTemplate restTemplate;

    @Value("${endpoints.eutoxrisk-file-validator}")
    private String url;

    private EUToxRiskFileValidator validator;

    @Before
    public void setup() {
        validator = new EUToxRiskFileValidator(restTemplate, url);
    }

    @Test
    public void testValidFile() {
        Collection<EUToxRiskFileValidationError> errors = validator.validate(
                ResourceHandler.getResourceFile("/input/toxrisk_datafile_valid.xlsx"));

        assertThat(errors).isEmpty();
    }

    @Test
    public void testInValidFile() {
        Collection<EUToxRiskFileValidationError> errors = validator.validate(
                ResourceHandler.getResourceFile("/input/toxrisk_datafile_invalid.xlsx"));

        assertThat(errors).hasSize(1);
    }
}
