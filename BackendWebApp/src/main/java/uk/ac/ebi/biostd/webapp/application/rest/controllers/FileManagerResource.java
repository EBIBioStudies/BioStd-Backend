package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileType;
import uk.ac.ebi.biostd.webapp.application.rest.mappers.FileMapper;
import uk.ac.ebi.biostd.webapp.application.rest.service.FileManagerService;
import uk.ac.ebi.biostd.webapp.application.security.service.MagicFolderUtil;

@RestController
@RequestMapping("/files")
@PreAuthorize("isAuthenticated()")
public class FileManagerResource {
    public static final String GROUP_FOLDER_NAME = "Groups";
    public static final String USER_FOLDER_NAME = "User";

    private final FileMapper fileMapper;
    private final MagicFolderUtil magicFolderUtil;
    private final FileManagerService fileManagerService;

    public FileManagerResource(
            FileMapper fileMapper, MagicFolderUtil magicFolderUtil, FileManagerService fileManagerService) {
        this.fileMapper = fileMapper;
        this.magicFolderUtil = magicFolderUtil;
        this.fileManagerService = fileManagerService;
    }

    @GetMapping("/user")
    @ResponseBody
    public FileDto getUserFiles(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "") String path) {
        String magicFolderPath = magicFolderUtil.getUserMagicFolderPath(user.getId(), user.getSecret());
        String fullPath = magicFolderPath + "/" +  path;
        FileDto userFolderDto = fileMapper.getCurrentFolderDto(USER_FOLDER_NAME, path, fullPath);
        userFolderDto.setFiles(fileMapper.map(fileManagerService.getUserFiles(user, path), USER_FOLDER_NAME, path));

        return userFolderDto;
    }

    @GetMapping("/groups")
    @ResponseBody
    public FileDto getGroupsFiles(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "") String path) {
        FileDto groupsFolderDto = createGroupsFolderDto();
        groupsFolderDto.setFiles(fileMapper.map(fileManagerService.getGroupsFiles(user, path), GROUP_FOLDER_NAME, path));

        return groupsFolderDto;
    }

    private FileDto createGroupsFolderDto() {
        FileDto groupsFolderDto = new FileDto();
        groupsFolderDto.setType(FileType.DIR);
        groupsFolderDto.setName(GROUP_FOLDER_NAME);
        groupsFolderDto.setPath("/" + GROUP_FOLDER_NAME);

        return groupsFolderDto;
    }
}
