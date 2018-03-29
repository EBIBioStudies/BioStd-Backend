package uk.ac.ebi.biostd.exporter.jobs.pmc.export.model;

import static javax.xml.bind.annotation.XmlAccessType.NONE;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(NONE)
@XmlRootElement(name = "link")
public class Link {

    @XmlAttribute(name = "providerId")
    private String providerId;

    @XmlElement(name = "resource")
    private Resource resource;

    @XmlElement(name = "record")
    private PmcRecord record;
}
