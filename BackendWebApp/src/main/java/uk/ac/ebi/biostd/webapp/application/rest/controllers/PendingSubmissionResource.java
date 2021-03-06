package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListFiltersDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitReportDto;
import uk.ac.ebi.biostd.webapp.application.rest.exceptions.ApiErrorException;
import uk.ac.ebi.biostd.webapp.application.rest.service.PendingSubmissionService;

@AllArgsConstructor
@RestController
@RequestMapping("/submissions/pending")
@PreAuthorize("isAuthenticated()")
public class PendingSubmissionResource {

    private final PendingSubmissionService pendingSubmissionService;

    @GetMapping
    @ResponseBody
    public PendingSubmissionListDto getSubmissionList(PendingSubmissionListFiltersDto filters,
            @AuthenticationPrincipal User user) {
        return pendingSubmissionService.getSubmissionList(filters, user);
    }

    @PostMapping
    @ResponseBody
    public PendingSubmissionDto createSubmission(@RequestBody ObjectNode pageTab, @AuthenticationPrincipal User user) {
        return pendingSubmissionService.createSubmission(pageTab, user);
    }

    @GetMapping("/{accno}")
    @ResponseBody
    public PendingSubmissionDto getSubmission(@PathVariable String accno, @AuthenticationPrincipal User user) {
        return pendingSubmissionService.getSubmissionByAccNo(accno, user)
                .orElseThrow(() -> pendingSubmissionNotFound(accno));
    }

    @DeleteMapping("/{accno}")
    public void deleteSubmission(@PathVariable String accno, @AuthenticationPrincipal User user) {
        pendingSubmissionService.deleteSubmissionByAccNo(accno, user);
    }

    @PutMapping("/{accno}")
    public PendingSubmissionDto updateSubmission(@PathVariable String accno, @RequestBody ObjectNode pageTab,
            @AuthenticationPrincipal User user) {
        return pendingSubmissionService.updateSubmission(accno, pageTab, user)
                .orElseThrow(() -> pendingSubmissionNotFound(accno));
    }

    @PostMapping("/{accno}/submit")
    @ResponseBody
    public SubmitReportDto submitSubmission(@PathVariable String accno, @RequestBody ObjectNode pageTab,
            @AuthenticationPrincipal User user) {
        return pendingSubmissionService.submitSubmission(accno, pageTab, user)
                .orElseThrow(() -> pendingSubmissionNotFound(accno));
    }

    private ApiErrorException pendingSubmissionNotFound(String accno) {
        return new ApiErrorException("Pending submission with accno=[" + accno + "] was not found", HttpStatus.BAD_REQUEST);
    }
}
