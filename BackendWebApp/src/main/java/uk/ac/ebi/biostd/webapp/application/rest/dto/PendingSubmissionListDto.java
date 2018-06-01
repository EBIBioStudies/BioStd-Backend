package uk.ac.ebi.biostd.webapp.application.rest.dto;

import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

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
