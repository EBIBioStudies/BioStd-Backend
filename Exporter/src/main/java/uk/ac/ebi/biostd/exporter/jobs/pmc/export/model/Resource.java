package uk.ac.ebi.biostd.exporter.jobs.pmc.export.model;

import static javax.xml.bind.annotation.XmlAccessType.NONE;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlAccessorType(NONE)
@XmlRootElement(name = "resource")
public class Resource {

    @XmlElement(name = "url")
    private String url;

    @XmlElement(name = "title")
    private String title;
}
