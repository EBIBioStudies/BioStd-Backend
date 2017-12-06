package uk.ac.ebi.biostd.exporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
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
    @XmlAttribute(name = "id")
    private long id;

    @JsonInclude(Include.NON_NULL)
    @JsonProperty("accno")
    @XmlAttribute(name = "acc")
    private String accNo;

    @JsonProperty("type")
    @XmlAttribute(name = "type")
    public String type;

    @JsonProperty("attributes")
    @JsonInclude(Include.NON_EMPTY)
    @XmlElementWrapper(name = "attributes")
    @XmlElement(name = "attribute")
    private List<Attribute> attributes;

    @JsonProperty("files")
    @JsonInclude(Include.NON_EMPTY)
    @XmlElementWrapper(name = "files")
    @XmlElement(name = "file")
    private List<File> files;

    @JsonProperty("links")
    @JsonInclude(Include.NON_EMPTY)
    private List<Link> links;

    @JsonIgnore
    @XmlElement(name = "links")
    public Links getXmlLinks() {
        return CollectionUtils.isEmpty(links) ? null : new Links(links);
    }

    @JsonInclude(Include.NON_EMPTY)
    @XmlElementWrapper(name = "subsections")
    @XmlElement(name = "section")
    private List<Section> subsections;


}
