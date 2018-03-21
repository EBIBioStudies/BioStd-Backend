package uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.services;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.dto.EUToxRiskFileValidationError;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.dto.EUToxRiskFileValidationResponse;

import java.io.File;
import java.util.Collection;

/**
 * @author Olga Melnichuk
 */
public class EUToxRiskFileValidator {

    private final RestTemplate restTemplate;
    private final String url;

    public EUToxRiskFileValidator(RestTemplate restTemplate, String url) {
        this.restTemplate = restTemplate;
        this.url = url;
    }

    public Collection<EUToxRiskFileValidationError> validate(File file) {
        FileSystemResource value = new FileSystemResource(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<FileSystemResource> requestEntity = new HttpEntity<>(value, headers);

        EUToxRiskFileValidationResponse resp = restTemplate.postForObject(url, requestEntity, EUToxRiskFileValidationResponse.class);
        return resp.getErrors();
    }
}
