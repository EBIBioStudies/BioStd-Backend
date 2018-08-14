package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileDto;
import uk.ac.ebi.biostd.webapp.application.rest.mappers.FileMapper;
import uk.ac.ebi.biostd.webapp.application.rest.service.FileManagerService;
import uk.ac.ebi.biostd.webapp.application.security.service.MagicFolderUtil;

@RestController
@AllArgsConstructor
@RequestMapping("/files")
@PreAuthorize("isAuthenticated()")
public class FileManagerResource {
    public static final String GROUP_FOLDER_NAME = "Groups";
    public static final String USER_FOLDER_NAME = "User";

    private final FileMapper fileMapper;
    private final MagicFolderUtil magicFolderUtil;
    private final FileManagerService fileManagerService;

    @GetMapping("/user")
    @ResponseBody
    public FileDto getUserFiles(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "") String path) {
        List<File> userFiles = fileManagerService.getUserFiles(user, path);
        Path magicFolderPath = magicFolderUtil.getUserMagicFolderPath(user.getId(), user.getSecret());

        return mapFiles(USER_FOLDER_NAME, magicFolderPath, path, userFiles);
    }

    @GetMapping("/groups")
    @ResponseBody
    public FileDto getGroupsFiles(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "") String path) {
        List<File> groupFiles = fileManagerService.getGroupsFiles(user, path);
        Path magicFolderPath = magicFolderUtil.getUserMagicFolderPath(user.getId(), user.getSecret());

        return mapFiles(GROUP_FOLDER_NAME, magicFolderPath, path, groupFiles);
    }

    private FileDto mapFiles(String basePath, Path magicFolderPath, String requestPath, List<File> files) {
        Path fullPath = magicFolderPath.resolve(requestPath);
        FileDto currentFolderDto = fileMapper.getCurrentFolderDto(basePath, requestPath, fullPath);
        currentFolderDto.setFiles(fileMapper.map(files, basePath, requestPath));

        return currentFolderDto;
    }
}
