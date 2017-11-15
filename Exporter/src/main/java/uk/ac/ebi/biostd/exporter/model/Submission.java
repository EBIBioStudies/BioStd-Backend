package uk.ac.ebi.biostd.exporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonPropertyOrder({"id", "accno", "title", "seckey", "relPath", "rtime", "ctime", "mtime",
        "type", "accessTags", "attributes", "section"})
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

    @JsonIgnore
    private long owner_id;

    @JsonProperty("type")
    public String type() {
        return SUBMISSION_TYPE;
    }

    @JsonIgnore
    private long rootSection_id;

    @JsonProperty("accessTags")
    private List<String> accessTags;

    @JsonProperty("attributes")
    private List<Attribute> attributes;

    @JsonProperty("section")
    private Section section;
}
