package uk.ac.ebi.biostd.webapp.application.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class PendingSubmissionDto {

    private String accno;
    private long changed;

    private JsonNode data;

    @JsonIgnore
    public long getModificationTimeInSeconds() {
        return changed / 1000;
    }
}
