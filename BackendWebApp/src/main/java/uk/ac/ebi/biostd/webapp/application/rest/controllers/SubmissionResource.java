package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import java.beans.PropertyEditorSupport;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.domain.model.SubmissionFilter;
import uk.ac.ebi.biostd.webapp.application.domain.services.SubmissionDataService;
import uk.ac.ebi.biostd.webapp.application.rest.dto.BatchSubmitReportDto;
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

    @PostMapping("/submissions/batch/{operation}")
    public BatchSubmitReportDto batchCreateOrUpdate(@PathVariable SubmitOperation operation,
            @RequestParam List<String> attachTo,
            @RequestParam("file") MultipartFile[] files,
            @AuthenticationPrincipal User user) {

        List<SubmitReportDto> reports =
                Arrays.stream(files)
                        .map(file -> submitService.createOrUpdateSubmission(file, attachTo, operation, user))
                        .collect(Collectors.toList());
        return new BatchSubmitReportDto(reports);
    }

    @InitBinder
    public void initBinder(final WebDataBinder webdataBinder) {
        webdataBinder.registerCustomEditor(SubmitOperation.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                SubmitOperation value = Arrays.stream(SubmitOperation.values())
                        .filter(v -> v.toString().equalsIgnoreCase(text))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Operation '" + text + "' is not known"));
                setValue(value);
            }
        });
    }

}
