package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.biostd.webapp.application.domain.model.SubmissionFilter;
import uk.ac.ebi.biostd.webapp.application.domain.services.SubmissionDataService;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmissionsDto;
import uk.ac.ebi.biostd.webapp.application.rest.mappers.SubmissionsMapper;

@AllArgsConstructor
@RestController
@PreAuthorize("isAuthenticated()")
public class SubmissionResource {

    private final SubmissionDataService submissionService;
    private final SubmissionsMapper submissionsMapper;

    @GetMapping("/sbmlist")
    public SubmissionsDto getSubmissionsDto(
            @ModelAttribute SubmissionFilter filter,
            @AuthenticationPrincipal uk.ac.ebi.biostd.authz.User user) {
        return submissionsMapper.toSubmissionsDto(submissionService.getSubmissionsByUser(user.getId(), filter));
    }
}
