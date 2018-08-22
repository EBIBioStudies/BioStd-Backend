package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ebi.biostd.webapp.application.rest.controllers.FileManagerResource.PATH_REQUIRED_ERROR_MSG;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileType;
import uk.ac.ebi.biostd.webapp.application.rest.mappers.FileMapper;
import uk.ac.ebi.biostd.webapp.application.rest.service.FileManagerService;
import uk.ac.ebi.biostd.webapp.application.security.service.GroupService;
import uk.ac.ebi.biostd.webapp.application.security.service.MagicFolderUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(FileManagerResource.class)
@AutoConfigureMockMvc(secure = false, addFilters = false)
public class FileManagerResourceTest {
    private static final long FILE_SIZE = 123456L;
    private static final String TEST_PATH = "folder1";
    private static final String TEST_GROUP = "Group 1";
    private static final String TEST_FILE_PATH = "folder1/file1.txt";
    private static final String USER_FILE_FULL_PATH = "User/folder1/file1.txt";
    private static final String GROUP_FILE_FULL_PATH = "Groups/Group 1/folder1/file1.txt";
    private static final String FOLDER_NAME = "folder1";
    private static final String FILE_NAME = "file1.txt";
    private static final String USER_FILES_ENDPOINT = "/files/user";
    private static final String NAMED_GROUP_FILES_ENDPOINT = "/files/groups/Group 1";
    private static final String CURRENT_USER_FOLDER_PATH = "/User/folder1";
    private static final String CURRENT_GROUP_FOLDER_PATH = "/Groups/folder1";

    @Rule
    public TemporaryFolder mockFileSystem = new TemporaryFolder();

    @Autowired
    private MockMvc mvc;

    @MockBean
    private FileMapper fileMapper;

    @MockBean
    private GroupService groupService;

    @MockBean
    private MagicFolderUtil magicFolderUtil;

    @MockBean
    private FileManagerService fileManagerService;

    private User user = new User();

    @Before
    public void onSetUp() throws Exception {
        mockFileSystem.newFolder(FOLDER_NAME);
        mockFileSystem.newFile(TEST_FILE_PATH);

        Authentication auth = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void getUserFiles() throws Exception {
        performGetFilesRequest(USER_FILES_ENDPOINT, CURRENT_USER_FOLDER_PATH);
    }

    @Test
    public void getUserSpecificFile() throws Exception {
        setUpMockFiles(CURRENT_USER_FOLDER_PATH, USER_FILE_FULL_PATH);
        mvc.perform(get(USER_FILES_ENDPOINT + "/" + TEST_FILE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(FILE_NAME))
                .andExpect(jsonPath("$.path").value(USER_FILE_FULL_PATH))
                .andExpect(jsonPath("$.size").value(FILE_SIZE))
                .andExpect(jsonPath("$.type").value(FileType.FILE.toString()));
    }

    @Test
    public void getGroupFiles() throws Exception {
        performGetFilesRequest(NAMED_GROUP_FILES_ENDPOINT, CURRENT_GROUP_FOLDER_PATH);
    }

    @Test
    public void uploadUserFiles() throws Exception {
        performMultipartFilesRequest(USER_FILES_ENDPOINT, CURRENT_USER_FOLDER_PATH);
    }

    @Test
    public void uploadGroupFiles() throws Exception {
        performMultipartFilesRequest(NAMED_GROUP_FILES_ENDPOINT, CURRENT_GROUP_FOLDER_PATH);
    }

    @Test
    public void deleteUserFile() throws Exception {
        performDeleteFilesRequest(USER_FILES_ENDPOINT, USER_FILE_FULL_PATH);
        verify(fileManagerService).deleteUserFile(any(User.class), eq(TEST_FILE_PATH));
    }

    @Test
    public void deleteGroupFile() throws Exception {
        performDeleteFilesRequest(NAMED_GROUP_FILES_ENDPOINT, GROUP_FILE_FULL_PATH);
        verify(fileManagerService).deleteGroupFile(any(User.class), eq(TEST_GROUP), eq(TEST_FILE_PATH));
    }

    @Test
    public void deleteWithNoPath() throws Exception {
        mvc.perform(delete(USER_FILES_ENDPOINT))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andExpect(jsonPath("$.message").value(PATH_REQUIRED_ERROR_MSG));
    }

    private void performDeleteFilesRequest(String endpoint, String path) throws Exception {
        setUpMockFiles("", path);
        mvc.perform(delete(endpoint + "/" + TEST_FILE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(FILE_NAME))
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.size").value(FILE_SIZE))
                .andExpect(jsonPath("$.type").value(FileType.FILE.toString()));
    }

    private void performGetFilesRequest(String endpoint, String currentFolderPath) throws Exception {
        performRequest(get(endpoint + "/" + TEST_PATH), currentFolderPath);
    }

    private void performMultipartFilesRequest(String endpoint, String currentFolderPath) throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(FILE_NAME, FILE_NAME, "text/plain", "".getBytes());
        performRequest(multipart(endpoint + "/" + TEST_PATH).file(mockFile), currentFolderPath);
    }

    private void performRequest(RequestBuilder request, String currentFolderPath) throws Exception {
        setUpMockFiles(currentFolderPath, "");
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(TEST_PATH))
                .andExpect(jsonPath("$.path").value(currentFolderPath))
                .andExpect(jsonPath("$.files").exists())
                .andExpect(jsonPath("$.files", hasSize(1)))
                .andExpect(jsonPath("$.files[0].name").value(FILE_NAME))
                .andExpect(jsonPath("$.files[0].size").value(FILE_SIZE))
                .andExpect(jsonPath("$.files[0].type").value(FileType.FILE.toString()));
    }

    private List<FileDto> createMockDtos() {
        FileDto fileDto = new FileDto();
        fileDto.setName(FILE_NAME);
        fileDto.setSize(FILE_SIZE);
        fileDto.setType(FileType.FILE);

        return Arrays.asList(fileDto);
    }

    private List<File> createMockFiles() {
        File mockFile = mock(File.class);
        when(mockFile.isFile()).thenReturn(true);
        when(mockFile.length()).thenReturn(FILE_SIZE);
        when(mockFile.getName()).thenReturn(FILE_NAME);

        return Arrays.asList(mockFile);
    }

    private void setUpMockFiles(String currentFolderPath, String specificFilePath) {
        Path mockPath = mock(Path.class);
        Path mockFullPath = Paths.get(mockFileSystem.getRoot() + "/" + TEST_PATH);
        Path mockSpecificFileFullPath = Paths.get(mockFileSystem.getRoot() + TEST_FILE_PATH);
        List<File> mockUserFiles = createMockFiles();
        FileDto currentFolder = new FileDto();
        FileDto mappedUserFile = new FileDto();

        mappedUserFile.setName(FILE_NAME);
        mappedUserFile.setSize(FILE_SIZE);
        mappedUserFile.setPath(specificFilePath);
        mappedUserFile.setType(FileType.FILE);

        currentFolder.setName(TEST_PATH);
        currentFolder.setPath(currentFolderPath);

        when(mockPath.resolve(TEST_PATH)).thenReturn(mockFullPath);
        when(mockPath.resolve(TEST_FILE_PATH)).thenReturn(mockSpecificFileFullPath);

        when(magicFolderUtil.getUserMagicFolderPath(anyLong(), isNull())).thenReturn(mockPath);
        when(magicFolderUtil.getGroupMagicFolderPath(anyLong(), isNull())).thenReturn(mockPath);

        when(groupService.getGroupMagicFolderPath(anyLong(), anyString())).thenReturn(mockPath);

        when(fileManagerService.getUserFiles(any(User.class), eq(TEST_PATH))).thenReturn(mockUserFiles);
        when(fileManagerService.getGroupsFiles(any(User.class), eq(TEST_PATH))).thenReturn(mockUserFiles);
        when(fileManagerService.getUserFiles(any(User.class), eq(TEST_FILE_PATH))).thenReturn(mockUserFiles);
        when(fileManagerService.uploadFiles(any(MultipartFile[].class), any(Path.class))).thenReturn(mockUserFiles);
        when(fileManagerService.getGroupFiles(any(User.class), anyString(), eq(TEST_PATH))).thenReturn(mockUserFiles);
        when(fileManagerService.deleteUserFile(any(User.class), eq(TEST_FILE_PATH))).thenReturn(mockUserFiles.get(0));
        when(fileManagerService.getGroupFiles(
                any(User.class), eq(TEST_GROUP), eq(TEST_FILE_PATH))).thenReturn(mockUserFiles);
        when(fileManagerService.deleteGroupFile(
                any(User.class), eq(TEST_GROUP), eq(TEST_FILE_PATH))).thenReturn(mockUserFiles.get(0));

        when(fileMapper.map(eq(mockUserFiles), anyString(), eq(TEST_PATH))).thenReturn(createMockDtos());
        when(fileMapper.map(eq(mockUserFiles.get(0)), eq(specificFilePath))).thenReturn(mappedUserFile);
        when(fileMapper.getCurrentFolderDto(anyString(), eq(TEST_PATH), eq(mockFullPath))).thenReturn(currentFolder);
    }
}
