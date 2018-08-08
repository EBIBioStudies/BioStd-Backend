package uk.ac.ebi.biostd.webapp.application.rest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.biostd.webapp.application.rest.service.FileManagerService.GROUP_FOLDER_NAME;
import static uk.ac.ebi.biostd.webapp.application.rest.service.FileManagerService.USER_FOLDER_NAME;

import com.google.common.collect.Sets;
import java.nio.file.NoSuchFileException;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.authz.UserGroup;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileType;
import uk.ac.ebi.biostd.webapp.application.rest.mappers.FileMapper;
import uk.ac.ebi.biostd.webapp.application.security.service.MagicFolderUtil;

@RunWith(MockitoJUnitRunner.class)
public class FileManagerServiceTest {
    private static final long TEST_GROUP_ID = 123L;
    private static final long TEST_USER_ID = 456L;
    private static final String TEST_GROUP_SECRET = "abc";
    private static final String TEST_GROUP_FOLDER = "def";
    private static final String TEST_USER_SECRET = "ghi";
    private static final String TEST_USER_FOLDER = "jkl";
    private static final String TEST_NON_EXISTING_FOLDER = "mno";
    private static final String TEST_GROUP_FILE_NAME = "testFile1.txt";
    private static final String TEST_USER_FILE_NAME = "testFile2.txt";
    private static final String TEST_GROUP_FILE_PATH = TEST_GROUP_FOLDER + "/" + TEST_GROUP_FILE_NAME;
    private static final String TEST_USER_FILE_PATH = TEST_USER_FOLDER + "/" + TEST_USER_FILE_NAME;

    @Rule
    public TemporaryFolder testNfs = new TemporaryFolder();

    @Spy
    private FileMapper mockFileMapper;

    @Mock
    private MagicFolderUtil mockMagicFolderUtil;

    @Mock
    private User mockUser;

    @Mock
    private UserGroup mockUserGroup;

    @InjectMocks
    private FileManagerService testInstance;

    @Before
    @SneakyThrows
    public void setUp() {
        testNfs.newFolder(TEST_GROUP_SECRET, TEST_GROUP_FOLDER);
        testNfs.newFolder(TEST_USER_SECRET, TEST_USER_FOLDER);
        testNfs.newFile(TEST_GROUP_SECRET + "/" + TEST_GROUP_FILE_PATH);
        testNfs.newFile(TEST_USER_SECRET + "/" + TEST_USER_FILE_PATH);

        when(mockUser.getId()).thenReturn(TEST_USER_ID);
        when(mockUser.getSecret()).thenReturn(TEST_USER_SECRET);
        when(mockUserGroup.getId()).thenReturn(TEST_GROUP_ID);
        when(mockUserGroup.getSecret()).thenReturn(TEST_GROUP_SECRET);
        when(mockUser.getGroups()).thenReturn(Sets.newHashSet(mockUserGroup));
        when(mockMagicFolderUtil.getGroupMagicFolderPath(
                TEST_GROUP_ID, TEST_GROUP_SECRET)).thenReturn(testNfs.getRoot() + "/" +  TEST_GROUP_SECRET);
        when(mockMagicFolderUtil.getUserMagicFolderPath(
                TEST_USER_ID, TEST_USER_SECRET)).thenReturn(testNfs.getRoot() + "/" +  TEST_USER_SECRET);
    }

    @Test
    public void testGetUserFiles() {
        FileDto testUserFiles = testInstance.getUserFiles(mockUser, "");
        FileDto testFile = testUserFiles.getFiles().get(0);

        assertThat(testUserFiles.getName()).isEqualTo(USER_FOLDER_NAME);
        assertThat(testUserFiles.getPath()).isEqualTo("/" + USER_FOLDER_NAME + "/");
        assertThat(testUserFiles.getFiles().size()).isEqualTo(1);
        assertThat(testFile.getName()).isEqualTo(TEST_USER_FOLDER);
        assertThat(testFile.getPath()).isEqualTo("/" + USER_FOLDER_NAME + "/" + TEST_USER_FOLDER);
        assertThat(testFile.getType()).isEqualTo(FileType.DIR);
    }

    @Test
    public void testGetUserInnerFolderFiles() {
        FileDto testUserFiles = testInstance.getUserFiles(mockUser, TEST_USER_FOLDER);
        FileDto testFile = testUserFiles.getFiles().get(0);

        assertThat(testUserFiles.getName()).isEqualTo(TEST_USER_FOLDER);
        assertThat(testUserFiles.getPath()).isEqualTo("/" + USER_FOLDER_NAME + "/" + TEST_USER_FOLDER + "/");
        assertThat(testUserFiles.getFiles().size()).isEqualTo(1);
        assertThat(testFile.getName()).isEqualTo(TEST_USER_FILE_NAME);
        assertThat(testFile.getPath()).isEqualTo("/" + USER_FOLDER_NAME + "/" + TEST_USER_FILE_PATH);
        assertThat(testFile.getType()).isEqualTo(FileType.FILE);
    }

    @Test(expected = NoSuchFileException.class)
    public void testGetUserFilesNonExistingPath() {
        testInstance.getUserFiles(mockUser, TEST_NON_EXISTING_FOLDER);
    }

    @Test
    public void testGetGroupFiles() {
        FileDto testGroupsFiles = testInstance.getGroupsFiles(mockUser, TEST_GROUP_FOLDER);
        FileDto testFile = testGroupsFiles.getFiles().get(0);

        assertThat(testGroupsFiles.getName()).isEqualTo(GROUP_FOLDER_NAME);
        assertThat(testGroupsFiles.getPath()).isEqualTo("/" + GROUP_FOLDER_NAME);
        assertThat(testGroupsFiles.getFiles().size()).isEqualTo(1);
        assertThat(testFile.getName()).isEqualTo(TEST_GROUP_FILE_NAME);
        assertThat(testFile.getPath()).isEqualTo("/" + GROUP_FOLDER_NAME + "/" + TEST_GROUP_FILE_PATH);
        assertThat(testFile.getType()).isEqualTo(FileType.FILE);
    }
}
