package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import static org.apache.commons.collections4.SetUtils.emptyIfNull;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.beans.PropertyEditorSupport;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.domain.model.SubmissionFilter;
import uk.ac.ebi.biostd.webapp.application.domain.services.SubmissionDataService;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmissionsDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitOperation;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitReportDto;
import uk.ac.ebi.biostd.webapp.application.rest.mappers.SubmissionsMapper;
import uk.ac.ebi.biostd.webapp.application.rest.service.SubmitService;

@AllArgsConstructor
@RestController
@PreAuthorize("isAuthenticated()")
public class SubmissionResource {

    private final SubmissionDataService submissionService;
    private final SubmissionsMapper submissionsMapper;
    private final SubmitService submitService;

    @GetMapping("/sbmlist")
    public SubmissionsDto getSubmissionsDto(
            @ModelAttribute SubmissionFilter filter,
            @AuthenticationPrincipal uk.ac.ebi.biostd.authz.User user) {
        return submissionsMapper.toSubmissionsDto(submissionService.getSubmissionsByUser(user.getId(), filter));
    }

    @PostMapping("/submissions/file_submit/{operation}")
    public SubmitReportDto fileSubmit(@PathVariable SubmitOperation operation,
            @RequestParam(required = false) Set<String> attachTo,
            @RequestParam(required = false) String accnoTemplate,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        return submitService.submit(file, emptyIfNull(attachTo), accnoTemplate, operation, user);
    }

    @PostMapping("/submissions/submit/{operation}")
    public SubmitReportDto submit(@PathVariable SubmitOperation operation,
            @RequestParam(required = false) Set<String> attachTo,
            @RequestParam(required = false) String accnoTemplate,
            @RequestBody ObjectNode pageTab,
            @AuthenticationPrincipal User user) {
        return submitService.submitJson(pageTab, emptyIfNull(attachTo), accnoTemplate, operation, user);
    }

    @InitBinder
    public void initBinder(final WebDataBinder webdataBinder) {
        webdataBinder.registerCustomEditor(SubmitOperation.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                setValue(SubmitOperation.fromString(text));
            }
        });
    }
}
