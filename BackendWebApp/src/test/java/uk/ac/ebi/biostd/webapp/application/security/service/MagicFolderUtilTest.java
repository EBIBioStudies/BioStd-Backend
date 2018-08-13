package uk.ac.ebi.biostd.webapp.application.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.biostd.webapp.application.security.service.MagicFolderUtil.USER_GROUP_DIR_PROP_NAME;

import java.io.File;
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
}

