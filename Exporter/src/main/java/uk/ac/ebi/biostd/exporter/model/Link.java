package uk.ac.ebi.biostd.exporter.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Data;

@Data
public class Link {

    @JsonIgnore
    private long id;
    private String url;

    private List<Attribute> attributes;
}
