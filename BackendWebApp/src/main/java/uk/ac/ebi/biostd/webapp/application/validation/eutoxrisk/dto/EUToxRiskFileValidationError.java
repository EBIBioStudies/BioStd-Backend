package uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Olga Melnichuk
 */
@Getter
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EUToxRiskFileValidationError {
    private String sheet;
    private int line;
    private int column;
    private String message;
}
