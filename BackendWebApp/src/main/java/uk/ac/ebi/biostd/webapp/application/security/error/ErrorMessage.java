package uk.ac.ebi.biostd.webapp.application.security.error;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorMessage {

    private String message;
    private String status;
}
