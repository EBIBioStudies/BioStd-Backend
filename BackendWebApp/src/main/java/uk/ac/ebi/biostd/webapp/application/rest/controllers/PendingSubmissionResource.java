package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListFiltersDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListDto;
import uk.ac.ebi.biostd.webapp.application.rest.service.PendingSubmissionService;

@AllArgsConstructor
@RestController
@PreAuthorize("isAuthenticated()")
public class PendingSubmissionResource {

    private final PendingSubmissionService pendingSubmissionService;

    @GetMapping("/submissions/pending")
    public @ResponseBody
    PendingSubmissionListDto getSubmissions(PendingSubmissionListFiltersDto filters,
                                            @AuthenticationPrincipal User user) {
        return pendingSubmissionService.getSubmissionList(filters, user);
    }

    @GetMapping("/submissions/pending/{accno}")
    public ResponseEntity<PendingSubmissionDto> getSubmission(@PathVariable String accno,
                                                              @AuthenticationPrincipal User user) {
        return pendingSubmissionService.getSubmissionByAccNo(accno, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @DeleteMapping("/submissions/pending/{accno}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable String accno,
                                                 @AuthenticationPrincipal User user) {
        pendingSubmissionService.deleteSubmissionByAccNo(accno, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/submissions/pending/{accno}")
    public @ResponseBody
    PendingSubmissionDto updateSubmission(@RequestBody PendingSubmissionDto request,
                                          @AuthenticationPrincipal User user) {
        return pendingSubmissionService.updateSubmission(request, user);
    }

    @PostMapping("/submissions/pending")
    public ResponseEntity<PendingSubmissionDto> createSubmission(@RequestBody String request,
                                                                 @AuthenticationPrincipal User user) {
        return pendingSubmissionService.createSubmission(request, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}