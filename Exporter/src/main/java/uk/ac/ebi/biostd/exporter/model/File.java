package uk.ac.ebi.biostd.exporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class File {

    private static final String DIRECTORY_TYPE = "directory";
    private static final String FILE_TYPE = "file";

    @JsonIgnore
    private long id;

    @JsonProperty("path")
    private String path;

    @JsonProperty("name")
    private String name;

    @JsonProperty("size")
    private long size;

    @JsonIgnore
    private boolean directory;

    @JsonProperty("type")
    public String getType() {
        return directory ? DIRECTORY_TYPE : FILE_TYPE;
    }

    @JsonProperty("attributes")
    private List<Attribute> attributes;
}
