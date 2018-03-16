package uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

/**
 * @author Olga Melnichuk
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EUToxRiskFileValidationError {

    private String sheet;
    private int line;
    private int column;
    private String message;

    public EUToxRiskFileValidationError() {
    }

    private EUToxRiskFileValidationError(String sheet, int line, int column, String message) {
        this.sheet = sheet;
        this.line = line;
        this.column = column;
        this.message = message;
    }

    public static EUToxRiskFileValidationError serverError(String msg) {
        return new EUToxRiskFileValidationError("unknown", 0, 0, msg);
    }

    @Override
    public String toString() {
        return "EUToxRiskFileValidationError{" +
                "sheet='" + sheet + '\'' +
                ", line=" + line +
                ", column=" + column +
                ", message='" + message + '\'' +
                '}';
    }
}
