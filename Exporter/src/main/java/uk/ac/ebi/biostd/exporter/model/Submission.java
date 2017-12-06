package uk.ac.ebi.biostd.exporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.ac.ebi.biostd.exporter.jobs.full.xml.AccessTagsXmlAdapter;

@Data
@NoArgsConstructor
@JsonPropertyOrder({"id", "accno", "title", "seckey", "relPath", "rtime", "ctime", "mtime", "type", "accessTags",
        "attributes", "section"})
@XmlRootElement(name = "submission")
@XmlAccessorType(XmlAccessType.NONE)
public class Submission {

    private static final String SUBMISSION_TYPE = "submission";

    @JsonProperty("id")
    @XmlAttribute(name = "id")
    private long id;

    @JsonProperty("accno")
    @XmlAttribute(name = "acc")
    private String accno;

    @JsonIgnore
    private String title;

    @JsonProperty("seckey")
    @XmlAttribute(name = "seckey")
    private String secretKey;

    @JsonProperty("relPath")
    @XmlAttribute(name = "relPath")
    private String relPath;

    @JsonProperty("rtime")
    @XmlAttribute(name = "rtime")
    private String rTime;

    @JsonProperty("ctime")
    @XmlAttribute(name = "ctime")
    private String cTime;

    @JsonProperty("mtime")
    @XmlAttribute(name = "mtime")
    private String mTime;

    @JsonIgnore
    private long owner_id;

    @JsonProperty("type")
    public String type() {
        return SUBMISSION_TYPE;
    }

    @JsonIgnore
    private long rootSection_id;

    @JsonProperty("accessTags")
    @XmlAttribute(name = "access")
    @XmlJavaTypeAdapter(AccessTagsXmlAdapter.class)
    private List<String> accessTags;

    @JsonProperty("attributes")
    @XmlElementWrapper(name = "attributes")
    @XmlElement(name = "attribute")
    private List<Attribute> attributes;

    @JsonProperty("section")
    @XmlElement(name = "section")
    private Section section;
}
