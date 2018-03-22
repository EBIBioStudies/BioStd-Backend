package uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.services;

import com.pri.util.collection.Collections;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;

@Service
@Slf4j
public class EUToxRiskFileValidatorService {

    private static final Pattern EXCEL = Pattern.compile(".*\\.xlsx");

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

    public boolean appliesToProjectId(String accno) {
        return properties.isEnabled() && properties.getProjectId().equals(accno);
    }

    public Collection<EUToxRiskFileValidationError> validateFirst(List<File> files) {
        Optional<File> file = files.stream()
                .filter(this::isExcelFile)
                .findFirst();

        if (file.isPresent()) {
            return validate(file.get());
        }
        return Collections.emptyList();
    }

    private Collection<EUToxRiskFileValidationError> validate(final File file) {
        Future<Collection<EUToxRiskFileValidationError>> future = taskExecutor.submit(() -> validator.validate(file));
        try {
            return future.get(VALIDATION_WAIT_TIME, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("EUToxRisk file validation error", e);
            return singletonList(EUToxRiskFileValidationError.serverError("Unexpected server error"));
        }
    }

    private boolean isExcelFile(File file) {
        return EXCEL.matcher(file.getName().toLowerCase()).matches();
    }
}
