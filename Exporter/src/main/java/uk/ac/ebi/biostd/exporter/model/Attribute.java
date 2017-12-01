package uk.ac.ebi.biostd.exporter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "attribute")
@XmlAccessorType(XmlAccessType.NONE)
public class Attribute {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "value")
    private String value;

    @JsonProperty("valqual")
    @JsonInclude(Include.NON_EMPTY)
    @XmlElement(name = "valqual")
    private String valueQualifierString;

    public Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
