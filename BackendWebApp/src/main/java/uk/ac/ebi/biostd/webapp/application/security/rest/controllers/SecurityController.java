package uk.ac.ebi.biostd.webapp.application.security.rest.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.common.utils.PlainFileFormat;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.LoginInformation;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.ProjectsDto;
import uk.ac.ebi.biostd.webapp.application.security.rest.mappers.ProjectMapper;
import uk.ac.ebi.biostd.webapp.application.security.services.AccessManager;

@RestController
@AllArgsConstructor
public class SecurityController {

    private final ProjectMapper projectMapper;
    private final AccessManager securityService;

    @GetMapping("/atthost")
    @PreAuthorize("isAuthenticated()")
    public ProjectsDto getProjects(@AuthenticationPrincipal User user) {
        return projectMapper.getProjectsDto(securityService.getAllowedProjects(user.getId()));
    }

    @PostMapping(value = "/checkAccess", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getUserPermissions(@ModelAttribute LoginInformation loginInformation) {
        return PlainFileFormat.asPlainFile(securityService.getPermissions(loginInformation));
    }
}
