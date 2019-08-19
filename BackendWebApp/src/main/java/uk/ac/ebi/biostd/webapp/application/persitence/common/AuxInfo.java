package uk.ac.ebi.biostd.webapp.application.persitence.common;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

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

    private static final String ORCID = null;

    @XmlElement(name = "param")
    private List<Parameter> parameters;

    public String getOrcid() {
        return emptyIfNull(parameters).stream()
                .filter(param -> param.getName().equals(ORCID))
                .map(Parameter::getName)
                .findFirst().orElse(EMPTY);
    }
}
