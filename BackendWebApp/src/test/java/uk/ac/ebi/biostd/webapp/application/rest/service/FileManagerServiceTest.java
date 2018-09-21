package uk.ac.ebi.biostd.webapp.application.rest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.biostd.webapp.application.rest.service.FileManagerService.DIRECTORY_DELETE_ERROR_MSG;

import com.google.common.collect.Sets;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.authz.UserGroup;
import uk.ac.ebi.biostd.commons.files.MagicFolderUtil;
import uk.ac.ebi.biostd.webapp.application.security.service.GroupService;

@RunWith(MockitoJUnitRunner.class)
public class FileManagerServiceTest {
    private static final long GROUP_ID = 123L;
    private static final long USER_ID = 456L;
    private static final String GROUP_NAME = "Group1";
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
    private MultipartFile mockMultipartFile;

    @Mock
    private User mockUser;

    @Mock
    private UserGroup mockUserGroup;

    @Mock
    private uk.ac.ebi.biostd.webapp.application.persitence.entities.UserGroup mockUserGroupEntity;

    @Mock
    private GroupService mockGroupService;

    private Path userFolderPath;

    private Path groupFolderPath;

    private MultipartFile[] multipartFiles;

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
        multipartFiles = new MultipartFile[]{ mockMultipartFile };

        when(mockUser.getId()).thenReturn(USER_ID);
        when(mockUser.getSecret()).thenReturn(USER_SECRET);
        when(mockUserGroup.getId()).thenReturn(GROUP_ID);
        when(mockUserGroup.getSecret()).thenReturn(GROUP_SECRET);
        when(mockUserGroupEntity.getId()).thenReturn(GROUP_ID);
        when(mockUserGroupEntity.getSecret()).thenReturn(GROUP_SECRET);
        when(mockUser.getGroups()).thenReturn(Sets.newHashSet(mockUserGroup));
        when(mockGroupService.getGroupFromUser(USER_ID, GROUP_NAME)).thenReturn(mockUserGroupEntity);
        when(mockMagicFolderUtil.getUserMagicFolderPath(USER_ID, USER_SECRET)).thenReturn(userFolderPath);
        when(mockMagicFolderUtil.getGroupMagicFolderPath(GROUP_ID, GROUP_SECRET)).thenReturn(groupFolderPath);
    }

    @Test
    public void getUserFiles() {
        List<File> userFiles = testInstance.getUserFiles(mockUser, "");
        assertUserFiles(userFiles, USER_FOLDER, true);
    }

    @Test
    public void getUserInnerFolderFiles() {
        List<File> userFiles = testInstance.getUserFiles(mockUser, USER_FOLDER);
        assertUserFiles(userFiles, USER_FILE_NAME, false);
   }

   @Test
   public void getUserSpecificFile() {
       List<File> userFiles = testInstance.getUserFiles(mockUser, USER_FILE_PATH);
       assertUserFiles(userFiles, USER_FILE_NAME, false);
   }

    @Test
    public void getUserFilesNonExistingPath() {
        assertThatExceptionOfType(NoSuchFileException.class).isThrownBy(
                () -> testInstance.getUserFiles(mockUser, NON_EXISTING_FOLDER));
    }

    @Test
    public void getNonExistingMagicFolderPath() {
        assertThatExceptionOfType(NoSuchFileException.class).isThrownBy(
                () -> testInstance.getMagicFolderFiles(Paths.get(NON_EXISTING_FOLDER)));
    }

    @Test
    public void getGroupFiles() {
        List<File> groupsFiles = testInstance.getGroupFiles(mockUser, GROUP_NAME, GROUP_FOLDER);
        assertThat(groupsFiles).hasSize(1);

        File groupFile = groupsFiles.get(0);
        assertThat(groupFile.getName()).isEqualTo(GROUP_FILE_NAME);
        assertThat(groupFile.isFile()).isTrue();
    }

    @Test
    public void getGroupsFiles() {
        List<File> groupsFiles = testInstance.getGroupsFiles(mockUser, GROUP_FOLDER);
        assertThat(groupsFiles).hasSize(1);

        File groupFile = groupsFiles.get(0);
        assertThat(groupFile.getName()).isEqualTo(GROUP_FILE_NAME);
        assertThat(groupFile.isFile()).isTrue();
    }

    @Test
    public void deleteUserFile() {
        File deletedFile = testInstance.deleteUserFile(mockUser, USER_FILE_PATH);
        assertDeletedFile(deletedFile, USER_FILE_NAME);
    }

    @Test
    public void deleteGroupFile() {
        File deletedFile = testInstance.deleteGroupFile(mockUser, GROUP_NAME, GROUP_FILE_PATH);
        assertDeletedFile(deletedFile, GROUP_FILE_NAME);
    }

    @Test
    public void deleteFolder() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
                () -> testInstance.deleteUserFile(mockUser, USER_FOLDER)).withMessage(DIRECTORY_DELETE_ERROR_MSG);
    }

    @Test
    public void uploadFiles() throws Exception {
        when(mockMultipartFile.getOriginalFilename()).thenReturn(USER_FILE_NAME);
        when(mockMultipartFile.getInputStream()).thenReturn(IOUtils.toInputStream(""));

        List<File> uploadFiles = testInstance.uploadFiles(multipartFiles, userFolderPath);
        assertThat(uploadFiles).hasSize(1);

        File uploadFile = uploadFiles.get(0);
        assertThat(Files.exists(uploadFile.toPath())).isTrue();
        assertThat(uploadFile.getName()).isEqualTo(USER_FILE_NAME);
    }

    @Test
    public void uploadBrokenFile() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(
                () -> testInstance.uploadFiles(multipartFiles, userFolderPath));
    }

    private void assertDeletedFile(File file, String fileName) {
        assertThat(file.getName()).isEqualTo(fileName);
        assertThat(file.exists()).isFalse();
    }

    private void assertUserFiles(List<File> userFiles, String fileName, boolean isDirectory) {
        assertThat(userFiles).hasSize(1);

        File userFolder = userFiles.get(0);
        assertThat(userFolder.getName()).isEqualTo(fileName);
        assertThat(userFolder.isDirectory()).isEqualTo(isDirectory);
    }
}
