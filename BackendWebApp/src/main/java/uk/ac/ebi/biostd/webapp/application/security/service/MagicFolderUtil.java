
package uk.ac.ebi.biostd.webapp.application.security.service;

import static java.lang.String.format;
import static java.nio.file.attribute.PosixFilePermissions.fromString;

import java.io.File;
import java.nio.file.Files;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.configuration.ConfigProperties;

/**
 * Helps to create secret folder for users and group wich make required create it parents folder too. Secret folder
 * path is defined by:
 *
 * Parent folder is the two first letters of secret.
 * Secret folder is the characters of the secrets from character 3 plus the entity type letter ('a' for users, 'b'
 * for groups) and entity id.
 *
 * so for example for user with secret abc-123 and id=50, secret path will be /ab/c-123-a50
 */
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

    public String getUserMagicFolderPath(long userId, String secret) {
        return getMagicFolderPath(userId, secret, "a");
    }

    public String getGroupMagicFolderPath(long groupId, String secret) {
        return getMagicFolderPath(groupId, secret, "b");
    }

    private String getMagicFolderPath(long id, String secret, String separator) {
        String parent = format("%s/%s", basePath, secret.substring(0, 2));
        return format("%s/%s-%s%d", parent, secret.substring(2), separator, id);
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
