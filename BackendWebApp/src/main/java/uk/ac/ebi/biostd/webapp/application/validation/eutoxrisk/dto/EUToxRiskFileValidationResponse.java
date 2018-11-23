package uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * @author Olga Melnichuk
 */
@Getter
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EUToxRiskFileValidationResponse {
    @JsonProperty("Errors")
    private Collection<EUToxRiskFileValidationError> errors;
}
