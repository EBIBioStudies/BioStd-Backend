package uk.ac.ebi.biostd.webapp.application.rest.dto;

import lombok.*;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class PendingSubmissionListDto {

    private List<PendingSubmissionListItemDto> submissions;
    private final String status = "OK";

    public PendingSubmissionListDto(List<PendingSubmissionListItemDto> submissions) {
        this.submissions = Collections.unmodifiableList(submissions);
    }

    public void setSubmissions(List<PendingSubmissionListItemDto> submissions) {
        this.submissions = Collections.unmodifiableList(submissions);
    }
}