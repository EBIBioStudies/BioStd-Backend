package uk.ac.ebi.biostd.webapp.application.rest.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserGroup;
import uk.ac.ebi.biostd.webapp.application.security.service.GroupService;
import uk.ac.ebi.biostd.webapp.application.security.service.MagicFolderUtil;

@Service
@AllArgsConstructor
public class FileManagerService {
    private final GroupService groupService;
    private final MagicFolderUtil magicFolderUtil;

    public List<File> getUserFiles(User user, String path) {
        Path magicFolderPath = magicFolderUtil.getUserMagicFolderPath(user.getId(), user.getSecret());
        return getFiles(magicFolderPath, path);
    }

    public List<File> getGroupFiles(User user, String groupName, String path) {
        UserGroup group = groupService.getGroupFromUser(user.getId(), groupName);
        Path magicFolderPath = magicFolderUtil.getGroupMagicFolderPath(group.getId(), group.getSecret());

        return getFiles(magicFolderPath, path);
    }

    public List<File> getGroupsFiles(User user, String path) {
        List<File> groupFiles = new ArrayList<>();
        user.getGroups().stream().forEach(group -> {
            Path magicFolderPath = magicFolderUtil.getGroupMagicFolderPath(group.getId(), group.getSecret());
            groupFiles.addAll(getFiles(magicFolderPath, path));
        });

        return groupFiles;
    }

    public List<File> uploadFiles(MultipartFile[] files, Path path) {
        return Stream.of(files).map(file -> uploadFile(file, path)).collect(Collectors.toList());
    }

    @SneakyThrows
    List<File> getMagicFolderFiles(Path filesPath) {
        return Files.list(filesPath).map(path -> new File(path.toUri())).collect(Collectors.toList());
    }

    @SneakyThrows
    private File uploadFile(MultipartFile file, Path path) {
        Path filePath = Paths.get(path.toString(), file.getOriginalFilename());
        Files.write(filePath, file.getBytes(), StandardOpenOption.CREATE);

        return new File(filePath.toUri());
    }

    @SneakyThrows
    private List<File> getFiles(Path magicFolderPath, String requestPath) {
        Path fullPath = magicFolderPath.resolve(requestPath);
        List<File> files;
        if (!Files.exists(fullPath)) {
            throw new NoSuchFileException(fullPath.toString());
        }

        if (Files.isDirectory(fullPath)) {
            files = getMagicFolderFiles(fullPath);
        } else {
            files = Arrays.asList(new File(fullPath.toUri()));
        }

        return files;
    }
}
