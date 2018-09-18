package uk.ac.ebi.biostd.commons.files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(TempDirectory.class)
@ExtendWith(MockitoExtension.class)
class MagicFolderUtilTest {

    private static final String USER_EMAIL = "user_email";

    private static final long TEST_ID = 123L;
    private static final String TEST_SECRET = "abc-123";

    private static String testFolderPath;
    private static String symlinksPath;

    private MagicFolderUtil testInstance;

    @BeforeAll
    static void beforeAll(@TempDir Path tempDir) {
        testFolderPath = tempDir.toAbsolutePath().toString();
    }

    @BeforeEach
    void setup() {
        symlinksPath = testFolderPath + "/symlinks";
        testInstance = new MagicFolderUtil(testFolderPath, symlinksPath);
    }

    @Test
    void createUserMagicFolder() {
        testInstance.createUserMagicFolder(50L, TEST_SECRET);

        File parentFolder = new File(testFolderPath, "ab");
        assertThat(parentFolder).exists();

        File magicFolder = new File(parentFolder, "c-123-a50");
        assertThat(magicFolder).exists();
    }

    @Test
    void createGroupMagicFolder() {
        testInstance.createGroupMagicFolder(40L, TEST_SECRET);

        File parentFolder = new File(testFolderPath, "ab");
        assertThat(parentFolder).exists();

        File magicFolder = new File(parentFolder, "c-123-b40");
        assertThat(magicFolder).exists();
    }

    @Test
    void getUserMagicFolderPath() {
        Path userMagicFolderPath = testInstance.getUserMagicFolderPath(TEST_ID, TEST_SECRET);
        assertThat(userMagicFolderPath).isEqualTo(getExpectedPath(MagicFolderUtil.USER_FOLDER_PREFIX));
    }

    @Test
    void getGroupMagicFolderPath() {
        Path userMagicFolderPath = testInstance.getGroupMagicFolderPath(TEST_ID, TEST_SECRET);
        assertThat(userMagicFolderPath).isEqualTo(getExpectedPath(MagicFolderUtil.GROUP_FOLDER_PREFIX));
    }

    @Test
    void createMagicFolderException() {
        MagicFolderUtil faultyTestInstance = new MagicFolderUtil("/folder", "symlinks");

        assertThatExceptionOfType(NoSuchFileException.class).isThrownBy(
                () -> faultyTestInstance.createUserMagicFolder(TEST_ID, TEST_SECRET));
    }

    @Test
    void createUserSymlink() throws Exception {
        Path source = Paths.get(testFolderPath + "/directory");
        source.toFile().mkdir();

        testInstance.createUserSymlink(source, USER_EMAIL);
        assertThat(Paths.get(symlinksPath + "/u/" + USER_EMAIL).toRealPath()).isEqualTo(source);
    }

    private Path getExpectedPath(String prefix) {
        return Paths.get(String.format("%s/ab/c-123-%s%d", testFolderPath, prefix, TEST_ID));
    }
}

