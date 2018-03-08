package uk.ac.ebi.biostd.webapp.application.security.rest.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectsDto {

    private List<ProjectDto> submissions;
    private String status = "OK";

    public ProjectsDto(List<ProjectDto> submissions) {
        this.submissions = submissions;
    }
}
