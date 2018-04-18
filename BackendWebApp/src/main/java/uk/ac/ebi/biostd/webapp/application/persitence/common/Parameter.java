package uk.ac.ebi.biostd.webapp.application.persitence.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.NONE)
public class Parameter {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "value")
    private String value;

    public Parameter(String[] pairs) {
        name = pairs[0];
        value = pairs.length > 1 ? pairs[1] : "";
    }
}
