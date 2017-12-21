package uk.ac.ebi.biostd.webapp.application.security.rest.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectsDto {

    private final List<ProjectDto> submissions;
    private final String status = "OK";
}
