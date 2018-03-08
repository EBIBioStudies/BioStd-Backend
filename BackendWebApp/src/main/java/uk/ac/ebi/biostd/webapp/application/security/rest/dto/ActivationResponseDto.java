package uk.ac.ebi.biostd.webapp.application.security.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ActivationResponseDto {

    private String message;
    private String status;
}
