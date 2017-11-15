package uk.ac.ebi.biostd.exporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonPropertyOrder({"accno", "type", "attributes", "files"})
public class Section {

    @JsonIgnore
    private long id;

    @JsonInclude(Include.NON_NULL)
    @JsonProperty("accno")
    private String accNo;

    @JsonProperty("type")
    public String type;

    @JsonProperty("attributes")
    @JsonInclude(Include.NON_EMPTY)
    private List<Attribute> attributes;

    @JsonProperty("files")
    @JsonInclude(Include.NON_EMPTY)
    private List<File> files;

    @JsonProperty("links")
    @JsonInclude(Include.NON_EMPTY)
    private List<Link> links;

    @JsonInclude(Include.NON_EMPTY)
    private List<Section> subsections;
}
