package uk.ac.ebi.biostd.webapp.application.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiErrorDto {
    private String message;
}
