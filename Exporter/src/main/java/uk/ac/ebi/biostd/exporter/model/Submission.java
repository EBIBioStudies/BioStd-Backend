package uk.ac.ebi.biostd.exporter.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonPropertyOrder({"id", "accno", "title", "seckey", "relPath", "rTime", "cTime", "mTime", "views", "type",
    "accessTags", "attributes", "section"})
@XmlRootElement(name = "submission")
@XmlAccessorType(XmlAccessType.NONE)
public class Submission {
    private static final String SUBMISSION_TYPE = "submission";

    @JsonProperty("id")
    @JsonInclude(NON_NULL)
    private Long id;

    @JsonProperty("accno")
    private String accno;

    @JsonIgnore
    private String title;

    @JsonProperty("seckey")
    @JsonInclude(NON_NULL)
    private String secretKey;

    @JsonProperty("relPath")
    @JsonInclude(NON_NULL)
    private String relPath;

    @JsonProperty("rtime")
    @JsonInclude(NON_NULL)
    private String rtime;

    @JsonProperty("ctime")
    @JsonInclude(NON_NULL)
    private String ctime;

    @JsonProperty("mtime")
    @JsonInclude(NON_NULL)
    private String mtime;

    @JsonProperty("views")
    @JsonInclude(NON_NULL)
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
    @JsonInclude(NON_EMPTY)
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
}
