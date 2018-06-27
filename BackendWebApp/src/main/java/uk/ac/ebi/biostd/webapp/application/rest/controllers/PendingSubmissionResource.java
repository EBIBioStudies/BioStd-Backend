package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.rest.dto.*;
import uk.ac.ebi.biostd.webapp.application.rest.exceptions.BadRequestException;
import uk.ac.ebi.biostd.webapp.application.rest.service.PendingSubmissionService;

@AllArgsConstructor
@RestController
@PreAuthorize("isAuthenticated()")
public class PendingSubmissionResource {

    private final PendingSubmissionService pendingSubmissionService;

    @GetMapping("/submissions/pending")
    @ResponseBody
    public PendingSubmissionListDto getSubmissions(PendingSubmissionListFiltersDto filters,
            @AuthenticationPrincipal User user) {
        return pendingSubmissionService.getSubmissionList(filters, user);
    }

    @GetMapping("/submissions/pending/{accno}")
    @ResponseBody
    public PendingSubmissionDto getSubmission(@PathVariable String accno,
            @AuthenticationPrincipal User user) {
        return pendingSubmissionService.getSubmissionByAccNo(accno, user)
                .orElseThrow(() -> pendingSubmissionNotFound(accno));
    }

    @DeleteMapping("/submissions/pending/{accno}")
    public void deleteSubmission(@PathVariable String accno,
            @AuthenticationPrincipal User user) {
        pendingSubmissionService.deleteSubmissionByAccNo(accno, user);
    }

    @PostMapping("/submissions/pending/{accno}")
    @ResponseBody
    public PendingSubmissionDto updateSubmission(@PathVariable String accno,
            @RequestBody ObjectNode pageTab,
            @AuthenticationPrincipal User user) {
        return pendingSubmissionService.updateSubmission(accno, pageTab, user)
                .orElseThrow(() -> pendingSubmissionNotFound(accno));
    }

    @PostMapping("/submissions/pending")
    @ResponseBody
    public PendingSubmissionDto createSubmission(@RequestBody ObjectNode pageTab,
            @AuthenticationPrincipal User user) {
        return pendingSubmissionService.createSubmission(pageTab, user);
    }

    @PostMapping("/submissions/pending/{accno}/submit")
    @ResponseBody
    public SubmitReportDto submitSubmission(@PathVariable String accno,
            @AuthenticationPrincipal User user) {
        return pendingSubmissionService.submitSubmission(accno, user)
                .orElseThrow(() -> pendingSubmissionNotFound(accno));
    }

    private BadRequestException pendingSubmissionNotFound(String accno) {
        return new BadRequestException("Pending submission with accno=[" + accno + "] was not found");
    }

    @ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<ApiErrorDto> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(new ApiErrorDto(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
