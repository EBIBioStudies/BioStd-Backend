package uk.ac.ebi.biostd.webapp.application.domain.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class SubmissionFilter {

    private Integer version;
    private Long rTimeFrom;
    private Long rTimeTo;
    private String accNo;
    private String keywords;
    private int limit;
    private int offset;

    public boolean hasVersion() {
        return version != null;
    }

    public boolean hasFromDate() {
        return rTimeFrom != null;
    }

    public boolean hasToDate() {
        return rTimeTo != null;
    }

    public boolean hasAccession() {
        return StringUtils.isNotBlank(accNo);
    }

    public boolean hasKeyWords() {
        return StringUtils.isNotBlank(keywords);
    }
}
