package uk.ac.ebi.biostd.webapp.application.rest.dto;

import lombok.Data;

@Data
public class PendingSubmissionListFiltersDto {
    private final Integer offset = 0;
    private final Integer limit = 50;
    private final String accNo;
    private final Long rTimeFrom;
    private final Long rTimeTo;
    private final String keywords;
}
