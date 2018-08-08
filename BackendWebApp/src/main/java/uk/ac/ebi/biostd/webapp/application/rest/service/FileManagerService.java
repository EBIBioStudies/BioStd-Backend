package uk.ac.ebi.biostd.webapp.application.rest.service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileType;
import uk.ac.ebi.biostd.webapp.application.rest.mappers.FileMapper;
import uk.ac.ebi.biostd.webapp.application.security.service.MagicFolderUtil;

@Service
public class FileManagerService {
    static final String GROUP_FOLDER_NAME = "Groups";
    static final String USER_FOLDER_NAME = "User";

    private final MagicFolderUtil magicFolderUtil;
    private final FileMapper fileMapper;

    public FileManagerService(MagicFolderUtil magicFolderUtil, FileMapper fileMapper) {
        this.magicFolderUtil = magicFolderUtil;
        this.fileMapper = fileMapper;
    }

    public FileDto getUserFiles(User user, String path) {
        String magicFolderPath = magicFolderUtil.getUserMagicFolderPath(user.getId(), user.getSecret());
        String fullPath = magicFolderPath + "/" +  path;
        FileDto userFolderDto = fileMapper.getCurrentFolderDto(USER_FOLDER_NAME, path, fullPath);
        userFolderDto.setFiles(getMagicFolderFiles(USER_FOLDER_NAME, magicFolderPath, path));

        return userFolderDto;
    }

    public FileDto getGroupsFiles(User user, String path) {
        final List<FileDto> groupFiles = new ArrayList<>();
        FileDto groupsFolderDto = createGroupsFolderDto();

        user.getGroups().stream().forEach(group -> {
            String magicFolderPath = magicFolderUtil.getGroupMagicFolderPath(group.getId(), group.getSecret());
            groupFiles.addAll(getMagicFolderFiles(GROUP_FOLDER_NAME, magicFolderPath, path));
        });
        groupsFolderDto.setFiles(groupFiles);

        return groupsFolderDto;
    }

    @SneakyThrows
    private List<FileDto> getMagicFolderFiles(String basePath, String magicFolderPath, String requestPath) {
        String fullPath = magicFolderPath + "/" +  requestPath;
        return Files.list(Paths.get(fullPath)).map(
                currentPath -> fileMapper.map(basePath, currentPath, requestPath)).collect(Collectors.toList());
    }

    private FileDto createGroupsFolderDto() {
        FileDto groupsFolderDto = new FileDto();
        groupsFolderDto.setType(FileType.DIR);
        groupsFolderDto.setName(GROUP_FOLDER_NAME);
        groupsFolderDto.setPath("/" + GROUP_FOLDER_NAME);

        return groupsFolderDto;
    }
}
