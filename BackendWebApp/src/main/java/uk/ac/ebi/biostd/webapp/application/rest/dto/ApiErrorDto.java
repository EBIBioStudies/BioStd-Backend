package uk.ac.ebi.biostd.webapp.application.rest.dto;

import lombok.Data;

@Data
public class ApiErrorDto {

    private String message;

    public ApiErrorDto(String message) {
        this.message = message;
    }
}
