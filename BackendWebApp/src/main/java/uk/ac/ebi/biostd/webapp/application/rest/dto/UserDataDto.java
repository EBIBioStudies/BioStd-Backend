package uk.ac.ebi.biostd.webapp.application.rest.dto;

import lombok.Data;

@Data
public class UserDataDto {

    private String dataKey;
    private Long userId;
    private String data;
    private String topic;
    private String contentType;
}
