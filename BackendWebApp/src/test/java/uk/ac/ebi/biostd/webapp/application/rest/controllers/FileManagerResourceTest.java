package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.FileType;
import uk.ac.ebi.biostd.webapp.application.rest.mappers.FileMapper;
import uk.ac.ebi.biostd.webapp.application.rest.service.FileManagerService;
import uk.ac.ebi.biostd.webapp.application.security.service.MagicFolderUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(FileManagerResource.class)
@AutoConfigureMockMvc(secure = false, addFilters = false)
public class FileManagerResourceTest {
    private static final long FILE_SIZE = 123456L;
    private static final String TEST_PATH = "/folder1";
    private static final String FILE_NAME = "file1.txt";
    private static final String CURRENT_USER_FOLDER_PATH = "/User/folder1";
    private static final String CURRENT_GROUP_FOLDER_PATH = "/Groups/folder1";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private FileMapper fileMapper;

    @MockBean
    private MagicFolderUtil magicFolderUtil;

    @MockBean
    private FileManagerService fileManagerService;

    private User user = new User();

    @Before
    public void onSetUp() {
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void getUserFiles() throws Exception {
        performGetFilesRequest("/files/user", CURRENT_USER_FOLDER_PATH);
    }

    @Test
    public void getGroupFiles() throws Exception {
        performGetFilesRequest("/files/groups", CURRENT_GROUP_FOLDER_PATH);
    }

    private void performGetFilesRequest(String endpoint, String currentFolderPath) throws Exception {
        setUpMockFiles(currentFolderPath);
        mvc.perform(get(endpoint)
                .param("path", TEST_PATH))
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

    private void setUpMockFiles(String currentFolderPath) {
        Path mockPath = mock(Path.class);
        Path mockFullPath = mock(Path.class);
        List<File> mockUserFiles = createMockFiles();
        FileDto currentFolder = new FileDto();

        currentFolder.setName(TEST_PATH);
        currentFolder.setPath(currentFolderPath);
        when(mockPath.resolve(TEST_PATH)).thenReturn(mockFullPath);
        when(magicFolderUtil.getUserMagicFolderPath(anyLong(), isNull())).thenReturn(mockPath);
        when(magicFolderUtil.getGroupMagicFolderPath(anyLong(), isNull())).thenReturn(mockPath);
        when(fileManagerService.getUserFiles(any(User.class), eq(TEST_PATH))).thenReturn(mockUserFiles);
        when(fileManagerService.getGroupsFiles(any(User.class), eq(TEST_PATH))).thenReturn(mockUserFiles);
        when(fileMapper.map(eq(mockUserFiles), anyString(), eq(TEST_PATH))).thenReturn(createMockDtos());
        when(fileMapper.getCurrentFolderDto(anyString(), eq(TEST_PATH), eq(mockFullPath))).thenReturn(currentFolder);
    }
}
