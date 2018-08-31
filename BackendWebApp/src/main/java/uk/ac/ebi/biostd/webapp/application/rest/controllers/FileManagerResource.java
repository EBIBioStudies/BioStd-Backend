package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import static uk.ac.ebi.biostd.webapp.application.rest.util.FileUtil.PATH_SEPARATOR;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import lombok.AllArgsConstructor;
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
import uk.ac.ebi.biostd.webapp.application.rest.mappers.FileMapper;
import uk.ac.ebi.biostd.webapp.application.rest.service.FileManagerService;
import uk.ac.ebi.biostd.webapp.application.rest.types.PathDescriptor;
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

    @GetMapping("/user/**")
    public List<FileDto> getUserFiles(
            PathDescriptor pathDescriptor,
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "false") String showArchive) {
        String path = pathDescriptor.getPath();
        List<File> userFiles = fileManagerService.getUserFiles(user, path);

        return mapFiles(userFiles, USER_FOLDER_NAME, path, pathDescriptor.getArchivePath(), showArchive);
    }

    @GetMapping("/groups/{groupName}/**")
    public List<FileDto> getGroupFiles(
            PathDescriptor pathDescriptor,
            @AuthenticationPrincipal User user,
            @PathVariable String groupName,
            @RequestParam(required = false, defaultValue = "false") String showArchive) {
        String path = pathDescriptor.getPath();
        String pathPrefix = GROUP_FOLDER_NAME + PATH_SEPARATOR + groupName;
        List<File> groupFiles = fileManagerService.getGroupFiles(user, groupName, path);

        return mapFiles(groupFiles, pathPrefix, path, pathDescriptor.getArchivePath(), showArchive);
    }

    @PostMapping("/user/**")
    public List<FileDto> uploadUserFiles(
            PathDescriptor pathDescriptor, @AuthenticationPrincipal User user, @RequestParam MultipartFile[] files) {
        Path magicFolderPath = magicFolderUtil.getUserMagicFolderPath(user.getId(), user.getSecret());
        return uploadFiles(
                USER_FOLDER_NAME, magicFolderPath, pathDescriptor.getPath(), files);
    }

    @PostMapping("/groups/{groupName}/**")
    public List<FileDto> uploadGroupFiles(
            PathDescriptor pathDescriptor,
            @AuthenticationPrincipal User user,
            @PathVariable String groupName,
            @RequestParam MultipartFile[] files) {
        Path magicFolderPath = groupService.getGroupMagicFolderPath(user.getId(), groupName);
        return uploadFiles(
                GROUP_FOLDER_NAME + PATH_SEPARATOR + groupName, magicFolderPath, pathDescriptor.getPath(), files);
    }

    @DeleteMapping("/user/**")
    public FileDto deleteUserFile(PathDescriptor pathDescriptor, @AuthenticationPrincipal User user) {
        String path = pathDescriptor.getRequiredPath();
        File deletedFile = fileManagerService.deleteUserFile(user, path);

        return fileMapper.mapFile(deletedFile, USER_FOLDER_NAME, path);
    }

    @DeleteMapping("/groups/{groupName}/**")
    public FileDto deleteGroupFile(
            PathDescriptor pathDescriptor, @AuthenticationPrincipal User user, @PathVariable String groupName) {
        String path = pathDescriptor.getRequiredPath();
        File deletedFile = fileManagerService.deleteGroupFile(user, groupName, path);

        return fileMapper.mapFile(deletedFile, GROUP_FOLDER_NAME + PATH_SEPARATOR + groupName, path);
    }

    private List<FileDto> mapFiles(
            List<File> files, String pathPrefix, String path, String archivePath, String showArchive) {
        return Boolean.valueOf(showArchive) ?
                fileMapper.mapFilesShowingArchive(files, pathPrefix, path, archivePath) :
                fileMapper.mapFiles(files, pathPrefix, path);
    }

    private List<FileDto> uploadFiles(
            String basePath, Path magicFolderPath, String requestPath, MultipartFile[] files) {
        Path currentPath = magicFolderPath.resolve(requestPath);
        List<File> uploadedFiles = fileManagerService.uploadFiles(files, currentPath);

        return fileMapper.mapFiles(uploadedFiles, basePath, requestPath);
    }
}
