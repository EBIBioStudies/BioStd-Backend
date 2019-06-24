package uk.ac.ebi.biostd.exporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubmissionStats {
    @JsonProperty("filesCount")
    private int filesCount;

    @JsonIgnore
    private long filesSize;
}
