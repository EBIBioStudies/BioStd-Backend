package uk.ac.ebi.biostd.exporter.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.NONE)
public class Link {

    @JsonIgnore
    private long id;

    @XmlElement(name = "url")
    private String url;

    @XmlElementWrapper(name = "attributes")
    @XmlElement(name = "attribute")
    private List<Attribute> attributes;
}
