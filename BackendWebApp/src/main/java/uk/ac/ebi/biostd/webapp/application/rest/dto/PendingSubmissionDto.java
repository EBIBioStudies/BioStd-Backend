package uk.ac.ebi.biostd.webapp.application.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;

@Data
public class PendingSubmissionDto {

    private String accno;
    private long changed;

    @JsonRawValue
    private String data;

    @JsonIgnore
    public long getModificationTimeInSeconds() {
        return changed / 1000;
    }
}
