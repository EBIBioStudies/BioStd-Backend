
package uk.ac.ebi.biostd.webapp.application.security.service;

import static java.lang.String.format;
import static java.nio.file.attribute.PosixFilePermissions.fromString;

import java.io.File;
import java.nio.file.Files;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.configuration.ConfigProperties;

@Component
public class MagicFolderUtil {

    private static final String PARENT_PERM = "rwx--x---";
    private static final String MAGIC_FOLDER_PERM = "rwxrwx---";

    static final String USER_GROUP_DIR_PROP_NAME = "biostd.userGroupDir";

    private final String basePath;

    public MagicFolderUtil(ConfigProperties configProperties) {
        basePath = configProperties.get(USER_GROUP_DIR_PROP_NAME);
    }

    void createUserMagicFolder(long userId, String magicKey) {
        createMagicFolder(userId, magicKey, "a");
    }

    void createGroupMagicFolder(long groupId, String magicKey) {
        createMagicFolder(groupId, magicKey, "b");
    }

    @SneakyThrows
    private void createMagicFolder(long id, String secret, String separator) {
        File parent = new File(format("%s/%s", basePath, secret.substring(0, 2)));
        createIfNotExists(parent);
        Files.setPosixFilePermissions(parent.toPath(), fromString(PARENT_PERM));

        File magicFolder = new File(format("%s/%s-%s%d", parent.getAbsolutePath(), secret.substring(2), separator, id));
        createIfNotExists(magicFolder);
        Files.setPosixFilePermissions(magicFolder.toPath(), fromString(MAGIC_FOLDER_PERM));
    }

    @SneakyThrows
    private void createIfNotExists(File file) {
        if (!file.exists()) {
            Files.createDirectory(file.toPath());
        }
    }
}
