package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import static uk.ac.ebi.biostd.webapp.application.rest.mappers.FileMapper.PATH_SEPARATOR;

import java.io.File;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileDto;
import uk.ac.ebi.biostd.webapp.application.rest.exceptions.ApiErrorException;
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
    public static final String USER_RESOURCE_ID = "user";
    public static final String PATH_REQUIRED_ERROR_MSG = "A file path must be specified for this operation";

    private final FileMapper fileMapper;
    private final GroupService groupService;
    private final MagicFolderUtil magicFolderUtil;
    private final FileManagerService fileManagerService;

    @GetMapping("/user/**")
    public List<FileDto> getUserFiles(HttpServletRequest request, @AuthenticationPrincipal User user) {
        String path = getPath(request.getRequestURL().toString(), USER_RESOURCE_ID);
        List<File> userFiles = fileManagerService.getUserFiles(user, path);
        Path magicFolderPath = magicFolderUtil.getUserMagicFolderPath(user.getId(), user.getSecret());

        return mapFiles(USER_FOLDER_NAME, magicFolderPath, path, userFiles);
    }

    @GetMapping("/groups/{groupName}/**")
    public List<FileDto> getGroupFiles(
            HttpServletRequest request, @AuthenticationPrincipal User user, @PathVariable String groupName) {
        String path = getPath(request.getRequestURL().toString(), groupName);
        Path magicFolderPath = groupService.getGroupMagicFolderPath(user.getId(), groupName);
        List<File> groupFiles = fileManagerService.getGroupFiles(user, groupName, path);

        return mapFiles(GROUP_FOLDER_NAME + PATH_SEPARATOR + groupName, magicFolderPath, path, groupFiles);
    }

    @PostMapping("/user/**")
    public List<FileDto> uploadUserFiles(
            HttpServletRequest request, @AuthenticationPrincipal User user, @RequestParam MultipartFile[] files) {
        Path magicFolderPath = magicFolderUtil.getUserMagicFolderPath(user.getId(), user.getSecret());
        return uploadFiles(
                USER_FOLDER_NAME, magicFolderPath, getPath(request.getRequestURL().toString(), USER_RESOURCE_ID), files);
    }

    @PostMapping("/groups/{groupName}/**")
    public List<FileDto> uploadGroupFiles(
            HttpServletRequest request,
            @AuthenticationPrincipal User user,
            @PathVariable String groupName,
            @RequestParam MultipartFile[] files) {
        Path magicFolderPath = groupService.getGroupMagicFolderPath(user.getId(), groupName);
        return uploadFiles(
                GROUP_FOLDER_NAME + PATH_SEPARATOR + groupName,
                magicFolderPath,
                getPath(request.getRequestURL().toString(), groupName), files);
    }

    @DeleteMapping("/user/**")
    public FileDto deleteUserFile(HttpServletRequest request, @AuthenticationPrincipal User user) {
        String path = getRequiredPath(request.getRequestURL().toString(), USER_RESOURCE_ID);
        File deletedFile = fileManagerService.deleteUserFile(user, path);

        return fileMapper.map(deletedFile, USER_FOLDER_NAME + PATH_SEPARATOR + path);
    }

    @DeleteMapping("/groups/{groupName}/**")
    public FileDto deleteGroupFile(
            HttpServletRequest request, @AuthenticationPrincipal User user, @PathVariable String groupName) {
        String path = getRequiredPath(request.getRequestURL().toString(), groupName);
        File deletedFile = fileManagerService.deleteGroupFile(user, groupName, path);

        return fileMapper.map(deletedFile, GROUP_FOLDER_NAME + PATH_SEPARATOR + groupName + PATH_SEPARATOR + path);
    }

    private String getPath(String requestUrl, String separator) {
        StringBuilder pathSeparator = new StringBuilder();
        String decodedUrl = URLDecoder.decode(requestUrl);
        pathSeparator.append(PATH_SEPARATOR).append(separator).append(PATH_SEPARATOR);
        int pathSeparatorIdx = StringUtils.indexOf(decodedUrl.toLowerCase(), pathSeparator.toString().toLowerCase());

        return pathSeparatorIdx > -1 ? StringUtils.substring(decodedUrl, pathSeparatorIdx + pathSeparator.length()) : "";
    }

    private String getRequiredPath(String requestUrl, String separator) {
        String path = getPath(requestUrl, separator);

        if (StringUtils.isEmpty(path)) {
            throw new ApiErrorException(PATH_REQUIRED_ERROR_MSG, HttpStatus.METHOD_NOT_ALLOWED);
        }

        return path;
    }

    private List<FileDto> uploadFiles(
            String basePath, Path magicFolderPath, String requestPath, MultipartFile[] files) {
        Path currentPath = magicFolderPath.resolve(requestPath);
        List<File> uploadedFiles = fileManagerService.uploadFiles(files, currentPath);

        return mapFiles(basePath, magicFolderPath, requestPath, uploadedFiles);
    }

    private List<FileDto> mapFiles(String basePath, Path magicFolderPath, String requestPath, List<File> files) {
        Path fullPath = magicFolderPath.resolve(requestPath);
        List<FileDto> mappedFiles;

        if (Files.isDirectory(fullPath)) {
            mappedFiles = fileMapper.map(files, basePath, requestPath);
        } else {
            mappedFiles = Arrays.asList(fileMapper.map(files.get(0), basePath + PATH_SEPARATOR + requestPath));
        }

        return mappedFiles;
    }
}
