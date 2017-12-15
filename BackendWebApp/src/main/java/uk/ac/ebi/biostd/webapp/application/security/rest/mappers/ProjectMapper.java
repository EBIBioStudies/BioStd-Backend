package uk.ac.ebi.biostd.webapp.application.security.rest.mappers;


import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.Submission;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.ProjectDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.ProjectsDto;

@Component
public class ProjectMapper {

    public ProjectsDto getProjectsDto(List<Submission> projects) {
        return new ProjectsDto(projects.stream().map(this::getProjectDto).collect(Collectors.toList()));
    }

    private ProjectDto getProjectDto(Submission submission) {
        return ProjectDto.builder()
                .rstitle(submission.getTitle())
                .title(submission.getTitle())
                .rtime(submission.getRTime())
                .accno(submission.getAccNo())
                .ctime(submission.getCTime())
                .mtime(submission.getMTime())
                .id(submission.getId())
                .version(submission.getVersion())
                .type(submission.getRootSection().getType()).build();
    }
}
