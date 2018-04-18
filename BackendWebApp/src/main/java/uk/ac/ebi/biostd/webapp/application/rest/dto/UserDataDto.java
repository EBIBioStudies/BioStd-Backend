package uk.ac.ebi.biostd.webapp.application.rest.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;

@Data
public class UserDataDto {

    private String dataKey;
    private Long userId;

    @JsonRawValue
    private String data;
    private String topic;
    private String contentType;
}
