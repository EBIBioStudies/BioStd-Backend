package uk.ac.ebi.biostd.webapp.application.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PendingSubmissionListItemDto {
    private String accno;
    private String title;
    private Long rtime;
    private Long mtime;
}
