package uk.ac.ebi.biostd.webapp.application.rest.mappers;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.Submission;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmissionDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmissionsDto;

@Component
public class SubmissionsMapper {

    public SubmissionsDto toSubmissionsDto(List<Submission> submissions) {
        List<SubmissionDto> submissionDtos = submissions.stream()
                .map(this::fromSubmission)
                .collect(Collectors.toList());

        return new SubmissionsDto("OK", submissionDtos);
    }

    private SubmissionDto fromSubmission(Submission submission) {
        SubmissionDto submissionDto = new SubmissionDto();
        submissionDto.setId(String.valueOf(submission.getId()));
        submissionDto.setAccno(submission.getAccNo());
        submissionDto.setTitle(submission.getTitle());
        submissionDto.setCtime(String.valueOf(submission.getCTime()));
        submissionDto.setRtime(String.valueOf(submission.getRTime()));
        submissionDto.setMtime(String.valueOf(submission.getMTime()));
        submissionDto.setVersion(String.valueOf(submission.getVersion()));
        return submissionDto;
    }
}
