
package uk.ac.ebi.biostd.commons.files;

import static java.lang.String.format;
import static java.nio.file.attribute.PosixFilePermissions.fromString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class MagicFolderUtil {

    public static final String USER_GROUP_DIR_PROP_NAME = "biostd.userGroupDir";

    private static final String PARENT_PERM = "rwx--x---";
    private static final String MAGIC_FOLDER_PERM = "rwxrwx---";

    static final String USER_FOLDER_PREFIX = "a";
    static final String GROUP_FOLDER_PREFIX = "b";

    private final String basePath;
    private final String symLinkPath;

    public MagicFolderUtil(String basePath, String symLinkPath) {
        this.basePath = basePath;
        this.symLinkPath = symLinkPath;
    }

    public Path createUserMagicFolder(long userId, String magicKey) {
        return createMagicFolder(userId, magicKey, USER_FOLDER_PREFIX);
    }

    public Path createGroupMagicFolder(long groupId, String magicKey) {
        return createMagicFolder(groupId, magicKey, GROUP_FOLDER_PREFIX);
    }

    public Path getUserMagicFolderPath(long userId, String secret) {
        return getMagicFolderPath(userId, secret, USER_FOLDER_PREFIX);
    }

    public Path getGroupMagicFolderPath(long groupId, String secret) {
        return getMagicFolderPath(groupId, secret, GROUP_FOLDER_PREFIX);
    }

    public void createUserSymlink(Path source, String userEmail) {
        try {
            File symLinkFile = new File(getSymLinkPath(symLinkPath, userEmail.toLowerCase()));
            symLinkFile.getParentFile().mkdirs();

            Path symLinkPath = symLinkFile.toPath();

            if (!Files.exists(symLinkPath)) {
                Files.createSymbolicLink(symLinkPath, source);
                log.info("creating symbolic link to {} in {}", source, symLinkFile);
            }
        } catch (IOException e) {
            log.error("Could not create symbolic link path", e);
        }
    }

    private String getSymLinkPath(String symlinkPath, String userEmail) {
        String prefixFolder = userEmail.substring(0, 1).toLowerCase();
        return format("%s/%s/%s", symlinkPath, prefixFolder, userEmail);
    }


    private Path getMagicFolderPath(long id, String secret, String separator) {
        String parent = format("%s/%s", basePath, secret.substring(0, 2));
        return Paths.get(format("%s/%s-%s%d", parent, secret.substring(2), separator, id));
    }

    @SneakyThrows
    private Path createMagicFolder(long id, String secret, String separator) {
        File parent = new File(format("%s/%s", basePath, secret.substring(0, 2)));
        createIfNotExists(parent);
        Files.setPosixFilePermissions(parent.toPath(), fromString(PARENT_PERM));

        File magicFolder = new File(format("%s/%s-%s%d", parent.getAbsolutePath(), secret.substring(2), separator, id));
        createIfNotExists(magicFolder);
        Files.setPosixFilePermissions(magicFolder.toPath(), fromString(MAGIC_FOLDER_PERM));

        return magicFolder.toPath();
    }

    @SneakyThrows
    private void createIfNotExists(File file) {
        if (!file.exists()) {
            Files.createDirectory(file.toPath());
        }
    }
}
