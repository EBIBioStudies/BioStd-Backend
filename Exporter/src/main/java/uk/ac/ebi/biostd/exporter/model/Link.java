package uk.ac.ebi.biostd.exporter.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.NONE)
public class Link {

    @JsonIgnore
    private long id;

    @XmlElement(name = "url")
    private String url;

    @XmlElement(name = "attributes")
    private List<Attribute> attributes;
}
