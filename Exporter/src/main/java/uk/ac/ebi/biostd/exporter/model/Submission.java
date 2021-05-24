package uk.ac.ebi.biostd.exporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonPropertyOrder({"id", "accno", "title", "seckey", "relPath", "rtime", "ctime", "mtime", "views", "type",
    "accessTags", "attributes", "section"})
@XmlRootElement(name = "submission")
@XmlAccessorType(XmlAccessType.NONE)
public class Submission {
    private static final String SUBMISSION_TYPE = "submission";

    @JsonProperty("id")
    private long id;

    @JsonProperty("accno")
    private String accno;

    @JsonIgnore
    private String title;

    @JsonProperty("seckey")
    private String secretKey;

    @JsonProperty("relPath")
    private String relPath;

    @JsonProperty("rtime")
    private String rTime;

    @JsonProperty("ctime")
    private String cTime;

    @JsonProperty("mtime")
    private String mTime;

    @JsonProperty("views")
    private Integer views;

    @JsonIgnore
    private long owner_id;

    @JsonProperty("type")
    public String type() {
        return SUBMISSION_TYPE;
    }

    @JsonProperty("filesCount")
    private int filesCount;

    @JsonIgnore
    private long rootSection_id;

    @JsonProperty("accessTags")
    private List<String> accessTags;

    @JsonProperty("attributes")
    private List<Attribute> attributes;

    @JsonProperty("section")
    private Section section;

    @JsonIgnore
    private boolean released;

    @JsonIgnore
    private boolean imagingSubmission;

    @JsonIgnore
    private long filesSize;

    @Override
    public Submission clone() {
        Submission cloned = new Submission();
        cloned.id = id;
        cloned.accno = accno;
        cloned.title = title;
        cloned.secretKey = secretKey;
        cloned.relPath = relPath;
        cloned.rTime = rTime;
        cloned.cTime = cTime;
        cloned.mTime = mTime;
        cloned.views = views;
        cloned.owner_id = owner_id;
        cloned.filesCount = filesCount;
        cloned.rootSection_id = rootSection_id;
        cloned.accessTags = accessTags;
        cloned.attributes = attributes;
        cloned.section = section;
        cloned.released = released;
        cloned.imagingSubmission = imagingSubmission;
        cloned.filesSize = filesSize;

        return cloned;
    }
}
