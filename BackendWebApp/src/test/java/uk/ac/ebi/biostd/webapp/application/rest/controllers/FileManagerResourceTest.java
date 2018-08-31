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
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ebi.biostd.webapp.application.rest.controllers.FileManagerResource.USER_FOLDER_NAME;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.request.AbstractParametersSnippet;
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
import uk.ac.ebi.biostd.webapp.application.rest.mappers.FileMapper;
import uk.ac.ebi.biostd.webapp.application.rest.service.FileManagerService;
import uk.ac.ebi.biostd.webapp.application.rest.types.FileType;
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
    private static final String TEST_GROUP_PATH = "Groups/Group 1";
    private static final String TEST_FILE_PATH = "folder1/file1.txt";
    private static final String TEST_ARCHIVE_PATH = "folder1/archive1.zip";
    private static final String FILE_NAME = "file1.txt";
    private static final String ARCHIVE_NAME = "archive1.txt";
    private static final String USER_FILES_ENDPOINT = "/files/user/{path}";
    private static final String USER_FILES_ENDPOINT_UPPERCASE = "/files/User";
    private static final String GROUP_FILES_ENDPOINT = "/files/groups/{groupName}/{path}";
    private static final String SHOW_ARCHIVE_QUERY_PARAM = "?showArchive=true";
    private static final String CURRENT_USER_FOLDER_PATH = "/User/folder1";
    private static final String CURRENT_GROUP_FOLDER_PATH = "/Groups/folder1";

    private static final String PATH_PARAM = "path";
    private static final String SHOW_ARCHIVE_PARAM = "showArchive";
    private static final String GROUP_NAME_PARAM = "groupName";
    private static final String GET_SPECIFIC_FILE_DOC_ID = "get-specific-file";
    private static final String GET_USER_FILES_DOC_ID = "get-user-files";
    private static final String GET_GROUP_FILES_DOC_ID = "get-group-files";
    private static final String UPLOAD_USER_FILES_DOC_ID = "upload-user-files";
    private static final String UPLOAD_GROUP_FILES_DOC_ID = "upload-group-files";
    private static final String DELETE_USER_FILES_DOC_ID = "delete-user-files";
    private static final String DELETE_GROUP_FILES_DOC_ID = "delete-group-files";
    private static final String GROUP_NAME_PARAM_DESC = "The group name";
    private static final String SHOW_ARCHIVE_PARAM_DESC = "If true, contents inside archive files is displayed";
    private static final String PATH_PARAM_SPECIFIC_FILE_DESC = "The path to the specific file";
    private static final String PATH_PARAM_DESC =
            "Path to a folder or specific file. If not provided, root folder files will be listed";
    private static final String PATH_PARAM_UPLOAD_DESC =
            "Path to a folder or specific file. If not provided, the files will be uploaded to root folder";

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
    public void setUp() {
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
        ResultActions testRequest = performArchiveRequest(TEST_ARCHIVE_PATH);
        generateDocs(
                testRequest,
                GET_SPECIFIC_FILE_DOC_ID,
                pathParameters(parameterWithName(PATH_PARAM).description(PATH_PARAM_SPECIFIC_FILE_DESC)),
                requestParameters(parameterWithName(SHOW_ARCHIVE_PARAM).description(SHOW_ARCHIVE_PARAM_DESC)));
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
                performSpecificFileRequest(delete(USER_FILES_ENDPOINT, TEST_FILE_PATH), USER_FOLDER_NAME, TEST_FILE_PATH);

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
                        delete(GROUP_FILES_ENDPOINT, TEST_GROUP, TEST_FILE_PATH), TEST_GROUP_PATH, TEST_FILE_PATH);

        verify(fileManagerService).deleteGroupFile(any(User.class), eq(TEST_GROUP), eq(TEST_FILE_PATH));
        generateDocs(
                testRequest,
                DELETE_GROUP_FILES_DOC_ID,
                pathParameters(
                    parameterWithName(GROUP_NAME_PARAM).description(GROUP_NAME_PARAM_DESC),
                    parameterWithName(PATH_PARAM).description(PATH_PARAM_SPECIFIC_FILE_DESC)));
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

    private ResultActions performArchiveRequest(String archivePath) throws Exception {
        setUpMockFiles(USER_FOLDER_NAME, archivePath);
        return mvc.perform(get(USER_FILES_ENDPOINT + SHOW_ARCHIVE_QUERY_PARAM, archivePath))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].name").value(ARCHIVE_NAME))
                .andExpect(jsonPath("$.[0].size").value(FILE_SIZE))
                .andExpect(jsonPath("$.[0].type").value(FileType.ARCHIVE.toString()))
                .andExpect(jsonPath("$.[0].files", hasSize(1)))
                .andExpect(jsonPath("$.[0].files[0].name").value(FILE_NAME))
                .andExpect(jsonPath("$.[0].files[0].size").value(FILE_SIZE))
                .andExpect(jsonPath("$.[0].files[0].type").value(FileType.FILE.toString()));
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
            ResultActions testRequest, String docId, AbstractParametersSnippet... parameters) throws Exception {
        for (AbstractParametersSnippet param : parameters) {
            testRequest.andDo(document(docId, param));
        }
        testRequest.andDo(document(docId, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));
    }

    private List<FileDto> createMockDtos() {
        FileDto fileDto = new FileDto();
        fileDto.setName(FILE_NAME);
        fileDto.setSize(FILE_SIZE);
        fileDto.setType(FileType.FILE);

        return Arrays.asList(fileDto);
    }

    private File createMockFile() {
        File mockFile = mock(File.class);
        when(mockFile.isFile()).thenReturn(true);
        when(mockFile.length()).thenReturn(FILE_SIZE);
        when(mockFile.getName()).thenReturn(FILE_NAME);

        return mockFile;
    }

    private void setUpMockFiles(String currentFolderPath, String specificFilePath) {
        Path mockPath = mock(Path.class);
        Path mockFullPath = mock(Path.class);
        File mockArchive = mock(File.class);
        File mockFile = createMockFile();
        List<File> mockFiles = Arrays.asList(mockFile);
        List<File> mockArchives = Arrays.asList(mockArchive);
        FileDto mappedFile = new FileDto();
        FileDto mappedArchive = new FileDto();

        mappedFile.setName(FILE_NAME);
        mappedFile.setSize(FILE_SIZE);
        mappedFile.setPath(specificFilePath);
        mappedFile.setType(FileType.FILE);

        mappedArchive.setName(ARCHIVE_NAME);
        mappedArchive.setSize(FILE_SIZE);
        mappedArchive.setType(FileType.ARCHIVE);
        mappedArchive.setFiles(Arrays.asList(mappedFile));

        when(mockPath.resolve(TEST_PATH)).thenReturn(mockFullPath);

        when(magicFolderUtil.getUserMagicFolderPath(anyLong(), isNull())).thenReturn(mockPath);

        when(groupService.getGroupMagicFolderPath(anyLong(), anyString())).thenReturn(mockPath);

        when(fileManagerService.getUserFiles(any(User.class), eq(""))).thenReturn(mockFiles);
        when(fileManagerService.getUserFiles(any(User.class), eq(TEST_PATH))).thenReturn(mockFiles);
        when(fileManagerService.getUserFiles(any(User.class), eq(TEST_ARCHIVE_PATH))).thenReturn(mockArchives);
        when(fileManagerService.uploadFiles(any(MultipartFile[].class), eq(mockFullPath))).thenReturn(mockFiles);
        when(fileManagerService.getGroupFiles(any(User.class), anyString(), eq(TEST_PATH))).thenReturn(mockFiles);
        when(fileManagerService.deleteUserFile(any(User.class), eq(TEST_FILE_PATH))).thenReturn(mockFile);
        when(fileManagerService.deleteGroupFile(any(User.class), eq(TEST_GROUP), eq(TEST_FILE_PATH))).thenReturn(mockFile);

        when(fileMapper.mapFile(mockFile, currentFolderPath, specificFilePath)).thenReturn(mappedFile);
        when(fileMapper.mapFiles(eq(mockFiles), anyString(), eq(""))).thenReturn(createMockDtos());
        when(fileMapper.mapFiles(eq(mockFiles), anyString(), eq(TEST_PATH))).thenReturn(createMockDtos());
        when(fileMapper
                .mapFilesShowingArchive(eq(mockArchives), eq(currentFolderPath), eq(specificFilePath), anyString()))
                .thenReturn(Arrays.asList(mappedArchive));
    }
}
