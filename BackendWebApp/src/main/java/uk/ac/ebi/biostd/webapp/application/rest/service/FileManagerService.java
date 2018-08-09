package uk.ac.ebi.biostd.webapp.application.rest.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.security.service.MagicFolderUtil;

@Service
public class FileManagerService {
    private final MagicFolderUtil magicFolderUtil;

    public FileManagerService(MagicFolderUtil magicFolderUtil) {
        this.magicFolderUtil = magicFolderUtil;
    }

    public List<File> getUserFiles(User user, String path) {
        String magicFolderPath = magicFolderUtil.getUserMagicFolderPath(user.getId(), user.getSecret());
        return getMagicFolderFiles(magicFolderPath, path);
    }

    public List<File> getGroupsFiles(User user, String path) {
        List<File> groupFiles = new ArrayList<>();
        user.getGroups().stream().forEach(group -> {
            String magicFolderPath = magicFolderUtil.getGroupMagicFolderPath(group.getId(), group.getSecret());
            groupFiles.addAll(getMagicFolderFiles(magicFolderPath, path));
        });

        return groupFiles;
    }

    @SneakyThrows
    private List<File> getMagicFolderFiles(String magicFolderPath, String requestPath) {
        String fullPath = magicFolderPath + "/" +  requestPath;
        return Files.list(Paths.get(fullPath)).map(path -> new File(path.toUri())).collect(Collectors.toList());
    }
}
