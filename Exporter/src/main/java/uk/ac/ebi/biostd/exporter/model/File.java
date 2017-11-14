package uk.ac.ebi.biostd.exporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class File {

    private static final String TYPE = "file";

    @JsonIgnore
    private long id;

    @JsonProperty("path")
    private String name;

    @JsonProperty("size")
    private long size;

    @JsonProperty("type")
    private String type() {
        return TYPE;
    }

    @JsonProperty("attributes")
    private List<Attribute> attributes;
}
