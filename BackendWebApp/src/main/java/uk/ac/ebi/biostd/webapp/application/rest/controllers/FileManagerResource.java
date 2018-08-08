package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import lombok.SneakyThrows;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileDto;
import uk.ac.ebi.biostd.webapp.application.rest.service.FileManagerService;

@RestController
@RequestMapping("/files")
@PreAuthorize("isAuthenticated()")
public class FileManagerResource {
    private final FileManagerService fileManagerService;

    public FileManagerResource(FileManagerService fileManagerService) {
        this.fileManagerService = fileManagerService;
    }

    @GetMapping("/user")
    @ResponseBody
    public FileDto getUserFolders(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "") String path) {
        return fileManagerService.getUserFiles(user, path);
    }

    @GetMapping("/groups")
    @ResponseBody
    public FileDto getGroupsFolders(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "") String path) {
        return fileManagerService.getGroupsFiles(user, path);
    }
}
