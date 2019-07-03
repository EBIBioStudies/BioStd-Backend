package uk.ac.ebi.biostd.exporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

@Data
@NoArgsConstructor
@JsonPropertyOrder({"accno", "type", "attributes", "files"})
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "section")
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

    @JsonIgnore
    public Links getXmlLinks() {
        return CollectionUtils.isEmpty(links) ? null : new Links(links);
    }

    @JsonInclude(Include.NON_EMPTY)
    private List<Section> subsections;
}
