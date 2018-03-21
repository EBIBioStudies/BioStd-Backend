package uk.ac.ebi.biostd.webapp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biostd.backend.testing.ResourceHandler;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.configuration.EUToxRiskFileValidatorConfig;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.configuration.EUToxRiskFileValidatorProperties;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.dto.EUToxRiskFileValidationError;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.services.EUToxRiskFileValidatorService;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.services.EUToxRiskFileValidator;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Olga Melnichuk
 */
@RunWith(SpringRunner.class)
@Import(EUToxRiskFileValidatorConfig.class)
public class EUToxRiskValidatorTest {

    private static final String VALID_XLSX_FILE = "/input/eutoxrisk_datafile_valid.xlsx";
    private static final String INVALID_XLSX_FILE = "/input/eutoxrisk_datafile_invalid.xlsx";

    @Autowired
    @Qualifier("eutoxrisk-file-validator")
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    @Qualifier("eutoxrisk-file-validator")
    private RestTemplate restTemplate;

    @Autowired
    private EUToxRiskFileValidatorProperties properties;

    private EUToxRiskFileValidator validator;

    @Before
    public void setup() {
        validator = new EUToxRiskFileValidator(restTemplate, properties.getEndpoint());
    }

    @Test
    public void testValidFile() {
        Collection<EUToxRiskFileValidationError> errors = validator.validate(
                ResourceHandler.getResourceFile(VALID_XLSX_FILE));

        assertThat(errors).isEmpty();
    }

    @Test
    public void testInvalidFile() {
        Collection<EUToxRiskFileValidationError> errors = validator.validate(
                ResourceHandler.getResourceFile(INVALID_XLSX_FILE));

        assertThat(errors).hasSize(1);
    }

    @Test
    public void testWithThreadPool() {
        EUToxRiskFileValidatorService service = new EUToxRiskFileValidatorService(properties, restTemplate, taskExecutor);
        Collection<EUToxRiskFileValidationError> errors = service.validate(
                ResourceHandler.getResourceFile(VALID_XLSX_FILE));

        assertThat(errors).isEmpty();
    }

}
