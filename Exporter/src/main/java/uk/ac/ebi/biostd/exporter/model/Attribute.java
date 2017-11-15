package uk.ac.ebi.biostd.exporter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attribute {

    private String name;
    private String value;

    @JsonProperty("valqual")
    @JsonInclude(Include.NON_EMPTY)
    private String valueQualifierString;

    public Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
