package uk.ac.ebi.biostd.webapp.application.rest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PendingSubmissionListDto {

    private List<PendingSubmissionListItemDto> submissions;
    private String status = "OK";

    public PendingSubmissionListDto(List<PendingSubmissionListItemDto> submissions) {
        this.submissions = submissions;
    }
}