package uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.configuration.EUToxRiskFileValidatorProperties;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.dto.EUToxRiskFileValidationError;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Collections.singletonList;

@Service
@Slf4j
public class EUToxRiskFileValidatorService {

    private static final int VALIDATION_WAIT_TIME = 40;

    private final EUToxRiskFileValidator validator;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final EUToxRiskFileValidatorProperties properties;

    @Autowired
    public EUToxRiskFileValidatorService(
            EUToxRiskFileValidatorProperties properties,
            @Qualifier("eutoxrisk-file-validator") RestTemplate restTemplate,
            @Qualifier("eutoxrisk-file-validator") ThreadPoolTaskExecutor taskExecutor) {
        this.validator = new EUToxRiskFileValidator(restTemplate, properties.getEndpoint());
        this.taskExecutor = taskExecutor;
        this.properties = properties;
    }

    public Collection<EUToxRiskFileValidationError> validate(final File file) {
        Future<Collection<EUToxRiskFileValidationError>> future = taskExecutor.submit(() -> validator.validate(file));
        try {
            return future.get(VALIDATION_WAIT_TIME, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("EUToxRisk file validation error", e);
            return singletonList(EUToxRiskFileValidationError.serverError("Unexpected server error"));
        }
    }

    public boolean matches(String accno) {
        return properties.isEnabled() && properties.getProjectId().equals(accno);
    }
}