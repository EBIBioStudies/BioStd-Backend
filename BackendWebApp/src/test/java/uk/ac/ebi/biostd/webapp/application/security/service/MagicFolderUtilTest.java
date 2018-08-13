package uk.ac.ebi.biostd.webapp.application.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.biostd.webapp.application.security.service.MagicFolderUtil.USER_GROUP_DIR_PROP_NAME;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ebi.biostd.webapp.application.configuration.ConfigProperties;

@RunWith(MockitoJUnitRunner.class)
public class MagicFolderUtilTest {
    private static final long TEST_ID = 123L;
    private static final String TEST_SECRET = "abc-123";

    @ClassRule
    public static TemporaryFolder TEST_FOLDER = new TemporaryFolder();

    private String rootPath;

    @Mock
    private ConfigProperties mockProperties;

    private MagicFolderUtil testInstance;

    @Before
    public void setup() {
        when(mockProperties.get(USER_GROUP_DIR_PROP_NAME)).thenReturn(TEST_FOLDER.getRoot().getAbsolutePath());
        testInstance = new MagicFolderUtil(mockProperties);

        rootPath = TEST_FOLDER.getRoot().getAbsolutePath();
    }

    @Test
    public void createUserMagicFolder() {
        testInstance.createUserMagicFolder(50L, "abc-123");

        File parentFolder = new File(rootPath, "ab");
        assertThat(parentFolder).exists();

        File magicFolder = new File(parentFolder, "c-123-a50");
        assertThat(magicFolder).exists();
    }

    @Test
    public void createGroupMagicFolder() {
        testInstance.createGroupMagicFolder(40L, "abc-123");

        File parentFolder = new File(rootPath, "ab");
        assertThat(parentFolder).exists();

        File magicFolder = new File(parentFolder, "c-123-b40");
        assertThat(magicFolder).exists();
    }

    @Test
    public void getUserMagicFolderPath() {
        Path userMagicFolderPath = testInstance.getUserMagicFolderPath(TEST_ID, TEST_SECRET);
        assertThat(userMagicFolderPath).isEqualTo(getExpectedPath(MagicFolderUtil.USER_FOLDER_PREFIX));
    }

    @Test
    public void getGroupMagicFolderPath() {
        Path userMagicFolderPath = testInstance.getGroupMagicFolderPath(TEST_ID, TEST_SECRET);
        assertThat(userMagicFolderPath).isEqualTo(getExpectedPath(MagicFolderUtil.GROUP_FOLDER_PREFIX));
    }

    @Test(expected = NoSuchFileException.class)
    public void createMagicFolderException() {
        when(mockProperties.get(USER_GROUP_DIR_PROP_NAME)).thenReturn("/folder");
        MagicFolderUtil faultyTestInstance = new MagicFolderUtil(mockProperties);

        faultyTestInstance.createUserMagicFolder(TEST_ID, TEST_SECRET);
    }

    private Path getExpectedPath(String prefix) {
        return Paths.get(String.format("%s/ab/c-123-%s%d", TEST_FOLDER.getRoot().getAbsolutePath(), prefix, TEST_ID));
    }
}

