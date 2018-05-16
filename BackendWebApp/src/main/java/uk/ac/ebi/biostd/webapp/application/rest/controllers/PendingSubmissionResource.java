package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmissionListFiltersDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListDto;
import uk.ac.ebi.biostd.webapp.application.rest.service.PendingSubmissionService;

@AllArgsConstructor
@RestController
@PreAuthorize("isAuthenticated()")
public class PendingSubmissionResource {

    private final PendingSubmissionService pendingSubmissionService;

    @GetMapping("/submissions/pending")
    public @ResponseBody PendingSubmissionListDto getSubmissions(
            SubmissionListFiltersDto filters,
            @AuthenticationPrincipal uk.ac.ebi.biostd.authz.User user) {

        return pendingSubmissionService.getSubmissions(filters, user);
    }
}