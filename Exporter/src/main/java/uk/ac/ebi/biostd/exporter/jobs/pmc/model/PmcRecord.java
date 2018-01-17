package uk.ac.ebi.biostd.exporter.jobs.pmc.model;

import static javax.xml.bind.annotation.XmlAccessType.NONE;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(NONE)
@XmlRootElement(name = "record")
public class PmcRecord {

    @XmlElement(name = "source")
    private String source;

    @XmlElement(name = "id")
    private String id;
}
