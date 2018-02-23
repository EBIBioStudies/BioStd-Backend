package uk.ac.ebi.biostd.webapp.application.persitence.aux;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlRootElement(name = "aux")
@XmlAccessorType(XmlAccessType.NONE)
public class AuxInfo {

    @XmlElement(name = "param")
    private List<Parameter> parameters;

}
