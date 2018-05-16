package uk.ac.ebi.biostd.webapp.application.rest.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class PendingSubmissionListItemDto {
    private String accno;
    private String title;
    private Long rtime;
    private Long mtime;
}