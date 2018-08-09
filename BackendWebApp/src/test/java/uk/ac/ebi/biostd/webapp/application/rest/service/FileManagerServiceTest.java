package uk.ac.ebi.biostd.webapp.application.rest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import java.io.File;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.authz.UserGroup;
import uk.ac.ebi.biostd.webapp.application.security.service.MagicFolderUtil;

@RunWith(MockitoJUnitRunner.class)
public class FileManagerServiceTest {
    private static final long GROUP_ID = 123L;
    private static final long USER_ID = 456L;
    private static final String GROUP_SECRET = "abc";
    private static final String GROUP_FOLDER = "def";
    private static final String USER_SECRET = "ghi";
    private static final String USER_FOLDER = "jkl";
    private static final String NON_EXISTING_FOLDER = "mno";
    private static final String GROUP_FILE_NAME = "testFile1.txt";
    private static final String USER_FILE_NAME = "testFile2.txt";
    private static final String GROUP_FILE_PATH = GROUP_FOLDER + "/" + GROUP_FILE_NAME;
    private static final String USER_FILE_PATH = USER_FOLDER + "/" + USER_FILE_NAME;

    @Rule
    public TemporaryFolder mockFileSystem = new TemporaryFolder();

    @Mock
    private MagicFolderUtil mockMagicFolderUtil;

    @Mock
    private User mockUser;

    @Mock
    private UserGroup mockUserGroup;

    private Path userFolderPath;

    private Path groupFolderPath;

    @InjectMocks
    private FileManagerService testInstance;

    @Before
    @SneakyThrows
    public void setUp() {
        mockFileSystem.newFolder(GROUP_SECRET, GROUP_FOLDER);
        mockFileSystem.newFolder(USER_SECRET, USER_FOLDER);
        mockFileSystem.newFile(GROUP_SECRET + "/" + GROUP_FILE_PATH);
        mockFileSystem.newFile(USER_SECRET + "/" + USER_FILE_PATH);
        userFolderPath = Paths.get(mockFileSystem.getRoot() + "/" +  USER_SECRET);
        groupFolderPath = Paths.get(mockFileSystem.getRoot() + "/" +  GROUP_SECRET);

        when(mockUser.getId()).thenReturn(USER_ID);
        when(mockUser.getSecret()).thenReturn(USER_SECRET);
        when(mockUserGroup.getId()).thenReturn(GROUP_ID);
        when(mockUserGroup.getSecret()).thenReturn(GROUP_SECRET);
        when(mockUser.getGroups()).thenReturn(Sets.newHashSet(mockUserGroup));
        when(mockMagicFolderUtil.getUserMagicFolderPath(USER_ID, USER_SECRET)).thenReturn(userFolderPath);
        when(mockMagicFolderUtil.getGroupMagicFolderPath(GROUP_ID, GROUP_SECRET)).thenReturn(groupFolderPath);
    }

    @Test
    public void getUserFiles() {
        List<File> userFiles = testInstance.getUserFiles(mockUser, "");
        assertThat(userFiles).hasSize(1);

        File userFolder = userFiles.get(0);
        assertThat(userFolder.getName()).isEqualTo(USER_FOLDER);
        assertThat(userFolder.isDirectory()).isTrue();
    }

    @Test
    public void getUserInnerFolderFiles() {
        List<File> userFiles = testInstance.getUserFiles(mockUser, USER_FOLDER);
        assertThat(userFiles).hasSize(1);

        File userFile = userFiles.get(0);
        assertThat(userFile.getName()).isEqualTo(USER_FILE_NAME);
        assertThat(userFile.isFile()).isTrue();
   }

    @Test(expected = NoSuchFileException.class)
    public void getUserFilesNonExistingPath() {
        testInstance.getUserFiles(mockUser, NON_EXISTING_FOLDER);
    }

    @Test
    public void getGroupFiles() {
        List<File> groupsFiles = testInstance.getGroupsFiles(mockUser, GROUP_FOLDER);
        assertThat(groupsFiles).hasSize(1);

        File groupFile = groupsFiles.get(0);
        assertThat(groupFile.getName()).isEqualTo(GROUP_FILE_NAME);
        assertThat(groupFile.isFile()).isTrue();
    }
}
