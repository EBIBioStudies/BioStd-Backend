package uk.ac.ebi.biostd.webapp.application.rest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PendingSubmissionListFiltersDto {
    private Integer offset = 0;
    private Integer limit = 50;
    private String accNo;
    private Long rTimeFrom;
    private Long rTimeTo;
    private String keywords;
}
