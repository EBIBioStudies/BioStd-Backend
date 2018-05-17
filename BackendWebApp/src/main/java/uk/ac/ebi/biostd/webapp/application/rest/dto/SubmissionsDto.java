package uk.ac.ebi.biostd.webapp.application.rest.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubmissionsDto {

    private String status;
    private List<SubmissionDto> submissions;
}
