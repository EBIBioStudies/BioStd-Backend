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
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.fileUpload;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
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
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
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
@AutoConfigureRestDocs(outputDir = "build/docs/snippets/files", uriHost = "biostudy-bia.ebi.ac.uk", uriPort = 8586)
public class FileManagerResourceTest {
    private static final long FILE_SIZE = 123456L;
    private static final String TEST_PATH = "folder1";
    private static final String TEST_GROUP = "Group 1";
    private static final String TEST_FILE_PATH = "folder1/file1.txt";
    private static final String USER_FILE_FULL_PATH = "User/folder1/file1.txt";
    private static final String GROUP_FILE_FULL_PATH = "Groups/Group 1/folder1/file1.txt";
    private static final String FOLDER_NAME = "folder1";
    private static final String FILE_NAME = "file1.txt";
    private static final String USER_FILES_ENDPOINT = "/files/user/{path}";
    private static final String USER_FILES_ENDPOINT_UPPERCASE = "/files/User";
    private static final String GROUP_FILES_ENDPOINT = "/files/groups/{groupName}/{path}";
    private static final String CURRENT_USER_FOLDER_PATH = "/User/folder1";
    private static final String CURRENT_GROUP_FOLDER_PATH = "/Groups/folder1";

    private static final String PATH_PARAM = "path";
    private static final String GROUP_NAME_PARAM = "groupName";
    private static final String GET_SPECIFIC_FILE_DOC_ID = "get-specific-file";
    private static final String GET_USER_FILES_DOC_ID = "get-user-files";
    private static final String GET_GROUP_FILES_DOC_ID = "get-group-files";
    private static final String UPLOAD_USER_FILES_DOC_ID = "upload-user-files";
    private static final String UPLOAD_GROUP_FILES_DOC_ID = "upload-group-files";
    private static final String DELETE_USER_FILES_DOC_ID = "delete-user-files";
    private static final String DELETE_GROUP_FILES_DOC_ID = "delete-group-files";
    private static final String GROUP_NAME_PARAM_DESC = "The group name";
    private static final String PATH_PARAM_SPECIFIC_FILE_DESC = "The path to the specific file";
    private static final String PATH_PARAM_DESC =
            "Path to a folder or specific file. If not provided, root folder files will be listed";
    private static final String PATH_PARAM_UPLOAD_DESC =
            "Path to a folder or specific file. If not provided, the files will be uploaded to root folder";

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
    public void setUp() throws Exception {
        mockFileSystem.newFolder(FOLDER_NAME);
        mockFileSystem.newFile(TEST_FILE_PATH);

        Authentication auth = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void getUserFiles() throws Exception {
        ResultActions testRequest = performRequest(get(USER_FILES_ENDPOINT, TEST_PATH), CURRENT_USER_FOLDER_PATH);
        generateDocs(
                testRequest,
                GET_USER_FILES_DOC_ID,
                pathParameters(parameterWithName(PATH_PARAM).description(PATH_PARAM_DESC).optional()));

    }

    @Test
    public void getUserFilesUppercaseWithNoPath() throws Exception {
        performRequest(get(USER_FILES_ENDPOINT_UPPERCASE), CURRENT_USER_FOLDER_PATH);
    }

    @Test
    public void getUserSpecificFile() throws Exception {
        ResultActions testRequest =
                performRequest(
                        get(USER_FILES_ENDPOINT, TEST_FILE_PATH), CURRENT_USER_FOLDER_PATH, USER_FILE_FULL_PATH);
        generateDocs(
                testRequest,
                GET_SPECIFIC_FILE_DOC_ID,
                pathParameters(parameterWithName(PATH_PARAM).description(PATH_PARAM_SPECIFIC_FILE_DESC)));
    }

    @Test
    public void getGroupFiles() throws Exception {
        ResultActions testRequest =
                performRequest(get(GROUP_FILES_ENDPOINT, TEST_GROUP, TEST_PATH), CURRENT_GROUP_FOLDER_PATH);

        generateDocs(
                testRequest,
                GET_GROUP_FILES_DOC_ID,
                pathParameters(
                    parameterWithName(GROUP_NAME_PARAM).description(GROUP_NAME_PARAM_DESC),
                    parameterWithName(PATH_PARAM).description(PATH_PARAM_DESC).optional()));
    }

    @Test
    public void uploadUserFiles() throws Exception {
        ResultActions testRequest =
                performFileUploadRequest(fileUpload(USER_FILES_ENDPOINT, TEST_PATH), CURRENT_USER_FOLDER_PATH);

        generateDocs(
                testRequest,
                UPLOAD_USER_FILES_DOC_ID,
                pathParameters(parameterWithName(PATH_PARAM).description(PATH_PARAM_UPLOAD_DESC).optional()));
    }

    @Test
    public void uploadGroupFiles() throws Exception {
        ResultActions testRequest =
                performFileUploadRequest(
                        fileUpload(GROUP_FILES_ENDPOINT, TEST_GROUP, TEST_PATH), CURRENT_GROUP_FOLDER_PATH);

        generateDocs(
                testRequest,
                UPLOAD_GROUP_FILES_DOC_ID,
                pathParameters(
                parameterWithName(GROUP_NAME_PARAM).description(GROUP_NAME_PARAM_DESC),
                parameterWithName(PATH_PARAM).description(PATH_PARAM_UPLOAD_DESC).optional()));
    }

    @Test
    public void deleteUserFile() throws Exception {
        ResultActions testRequest =
                performSpecificFileRequest(delete(USER_FILES_ENDPOINT, TEST_FILE_PATH), "", USER_FILE_FULL_PATH);

        verify(fileManagerService).deleteUserFile(any(User.class), eq(TEST_FILE_PATH));
        generateDocs(
                testRequest,
                DELETE_USER_FILES_DOC_ID,
                pathParameters(parameterWithName(PATH_PARAM).description(PATH_PARAM_SPECIFIC_FILE_DESC)));
    }

    @Test
    public void deleteGroupFile() throws Exception {
        ResultActions testRequest =
                performSpecificFileRequest(
                        delete(GROUP_FILES_ENDPOINT, TEST_GROUP, TEST_FILE_PATH), "", GROUP_FILE_FULL_PATH);

        verify(fileManagerService).deleteGroupFile(any(User.class), eq(TEST_GROUP), eq(TEST_FILE_PATH));
        generateDocs(
                testRequest,
                DELETE_GROUP_FILES_DOC_ID,
                pathParameters(
                    parameterWithName(GROUP_NAME_PARAM).description(GROUP_NAME_PARAM_DESC),
                    parameterWithName(PATH_PARAM).description(PATH_PARAM_SPECIFIC_FILE_DESC)));
    }

    @Test
    public void deleteWithNoPath() throws Exception {
        mvc.perform(delete(USER_FILES_ENDPOINT, ""))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .andExpect(jsonPath("$.message").value(PATH_REQUIRED_ERROR_MSG));
    }

    private ResultActions performFileUploadRequest(
            MockMultipartHttpServletRequestBuilder fileUploadRequest, String currentFolderPath) throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(FILE_NAME, FILE_NAME, "text/plain", "".getBytes());
        return performRequest(fileUploadRequest.file(mockFile), currentFolderPath);
    }

    private ResultActions performSpecificFileRequest(
            RequestBuilder request, String currentFolderPath, String specificFilePath) throws Exception {
        setUpMockFiles(currentFolderPath, specificFilePath);
        return mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(FILE_NAME))
                .andExpect(jsonPath("$.path").value(specificFilePath))
                .andExpect(jsonPath("$.size").value(FILE_SIZE))
                .andExpect(jsonPath("$.type").value(FileType.FILE.toString()));
    }

    private ResultActions performRequest(RequestBuilder request, String currentFolderPath) throws Exception {
        return performRequest(request, currentFolderPath, "");
    }

    private ResultActions performRequest(
            RequestBuilder request, String currentFolderPath, String specificFilePath) throws Exception {
        setUpMockFiles(currentFolderPath, specificFilePath);
       return mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].name").value(FILE_NAME))
                .andExpect(jsonPath("$.[0].size").value(FILE_SIZE))
                .andExpect(jsonPath("$.[0].type").value(FileType.FILE.toString()));
    }

    private void generateDocs(
            ResultActions testRequest, String docId, PathParametersSnippet pathParameters) throws Exception {
        testRequest.andDo(document(docId, pathParameters));
        testRequest.andDo(document(docId, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));
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

        when(mockPath.resolve("")).thenReturn(mockFullPath);
        when(mockPath.resolve(TEST_PATH)).thenReturn(mockFullPath);
        when(mockPath.resolve(TEST_FILE_PATH)).thenReturn(mockSpecificFileFullPath);

        when(magicFolderUtil.getUserMagicFolderPath(anyLong(), isNull())).thenReturn(mockPath);
        when(magicFolderUtil.getGroupMagicFolderPath(anyLong(), isNull())).thenReturn(mockPath);

        when(groupService.getGroupMagicFolderPath(anyLong(), anyString())).thenReturn(mockPath);

        when(fileManagerService.getUserFiles(any(User.class), eq(""))).thenReturn(mockUserFiles);
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

        when(fileMapper.map(eq(mockUserFiles), anyString(), eq(""))).thenReturn(createMockDtos());
        when(fileMapper.map(eq(mockUserFiles), anyString(), eq(TEST_PATH))).thenReturn(createMockDtos());
        when(fileMapper.map(eq(mockUserFiles.get(0)), eq(specificFilePath))).thenReturn(mappedUserFile);
    }
}
