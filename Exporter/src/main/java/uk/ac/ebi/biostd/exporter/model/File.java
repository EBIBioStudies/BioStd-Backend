package uk.ac.ebi.biostd.exporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlRootElement(name = "file")
@XmlAccessorType(XmlAccessType.NONE)
public class File {

    private static final String TYPE = "file";

    @JsonIgnore
    private long id;

    @JsonProperty("path")
    @XmlElement(name = "path")
    private String path;

    @JsonProperty("name")
    @XmlElement(name = "name")
    private String name;

    @JsonProperty("size")
    @XmlAttribute(name = "size")
    private long size;

    @JsonProperty("type")
    @XmlAttribute(name = "type")
    public String getType() {
        return TYPE;
    }

    @JsonProperty("attributes")
    @XmlElement(name = "attribute")
    @XmlElementWrapper(name = "attributes")
    private List<Attribute> attributes;
}
