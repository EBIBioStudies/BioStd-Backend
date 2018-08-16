package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileDto;
import uk.ac.ebi.biostd.webapp.application.rest.mappers.FileMapper;
import uk.ac.ebi.biostd.webapp.application.rest.service.FileManagerService;
import uk.ac.ebi.biostd.webapp.application.security.service.GroupService;
import uk.ac.ebi.biostd.webapp.application.security.service.MagicFolderUtil;

@RestController
@AllArgsConstructor
@RequestMapping("/files")
@PreAuthorize("isAuthenticated()")
public class FileManagerResource {
    public static final String GROUP_FOLDER_NAME = "Groups";
    public static final String USER_FOLDER_NAME = "User";

    private final FileMapper fileMapper;
    private final GroupService groupService;
    private final MagicFolderUtil magicFolderUtil;
    private final FileManagerService fileManagerService;

    @GetMapping("/user")
    public FileDto getUserFiles(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "") String path) {
        List<File> userFiles = fileManagerService.getUserFiles(user, path);
        Path magicFolderPath = magicFolderUtil.getUserMagicFolderPath(user.getId(), user.getSecret());

        return mapFiles(USER_FOLDER_NAME, magicFolderPath, path, userFiles);
    }

    @GetMapping("/groups")
    public FileDto getGroupsFiles(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "") String path) {
        List<File> groupFiles = fileManagerService.getGroupsFiles(user, path);
        Path magicFolderPath = magicFolderUtil.getUserMagicFolderPath(user.getId(), user.getSecret());

        return mapFiles(GROUP_FOLDER_NAME, magicFolderPath, path, groupFiles);
    }

    @GetMapping("/groups/{groupName}")
    public FileDto getGroupsFiles(
            @AuthenticationPrincipal User user,
            @PathVariable String groupName,
            @RequestParam(required = false, defaultValue = "") String path) {
        Path magicFolderPath = groupService.getGroupMagicFolderPath(user.getId(), groupName);
        List<File> groupFiles = fileManagerService.getGroupFiles(user, groupName, path);

        return mapFiles(GROUP_FOLDER_NAME, magicFolderPath, path, groupFiles);
    }

    @PostMapping("/user")
    public FileDto uploadUserFiles(
            @AuthenticationPrincipal User user,
            @RequestParam MultipartFile[] files,
            @RequestParam(required = false, defaultValue = "") String path) {
        Path magicFolderPath = magicFolderUtil.getUserMagicFolderPath(user.getId(), user.getSecret());
        return uploadFiles(USER_FOLDER_NAME, magicFolderPath, path, files);
    }

    @PostMapping("/groups/{groupName}")
    public FileDto uploadGroupFiles(
            @AuthenticationPrincipal User user,
            @PathVariable String groupName,
            @RequestParam MultipartFile[] files,
            @RequestParam(required = false, defaultValue = "") String path) {
        Path magicFolderPath = groupService.getGroupMagicFolderPath(user.getId(), groupName);
        return uploadFiles(GROUP_FOLDER_NAME, magicFolderPath, path, files);
    }

    private FileDto uploadFiles(
            String basePath, Path magicFolderPath, String requestPath, MultipartFile[] files) {
        Path currentPath = magicFolderPath.resolve(requestPath);
        List<File> uploadedFiles = fileManagerService.uploadFiles(files, currentPath);

        return mapFiles(basePath, magicFolderPath, requestPath, uploadedFiles);
    }

    private FileDto mapFiles(String basePath, Path magicFolderPath, String requestPath, List<File> files) {
        Path fullPath = magicFolderPath.resolve(requestPath);
        FileDto currentFolderDto = fileMapper.getCurrentFolderDto(basePath, requestPath, fullPath);
        currentFolderDto.setFiles(fileMapper.map(files, basePath, requestPath));

        return currentFolderDto;
    }
}
