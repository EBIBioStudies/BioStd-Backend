package uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.dto.EUToxRiskFileValidationError;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Collections.singletonList;

@Service
public class EUToxRiskFaileValidatorService {

    private EUToxRiskFileValidator validator;
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    public EUToxRiskFaileValidatorService(
            EUToxRiskFileValidator validator,
            @Qualifier("eutoxrisk-file-validator.TaskExecutor") ThreadPoolTaskExecutor taskExecutor) {
        this.validator = validator;
        this.taskExecutor = taskExecutor;
    }

    public Collection<EUToxRiskFileValidationError> validate(final File file) {
        Future<Collection<EUToxRiskFileValidationError>> future = taskExecutor.submit(() -> validator.validate(file));
        try {
            return future.get(40, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // TODO use logger
            e.printStackTrace();
            return singletonList(EUToxRiskFileValidationError.serverError("Unexpected server error"));
        }
    }
}
