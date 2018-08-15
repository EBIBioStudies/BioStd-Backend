package uk.ac.ebi.biostd.webapp.application.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceErrorDto {
    private String message;
    private String entity;
}
