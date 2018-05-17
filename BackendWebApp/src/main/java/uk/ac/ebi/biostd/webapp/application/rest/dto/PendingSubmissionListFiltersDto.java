package uk.ac.ebi.biostd.webapp.application.rest.dto;

import lombok.Data;

@Data
public class PendingSubmissionListFiltersDto {
    private final Integer offset;
    private final Integer limit;
    private final String accNo;
    private final Long rTimeFrom;
    private final Long rTimeTo;
    private final String keywords;
}
