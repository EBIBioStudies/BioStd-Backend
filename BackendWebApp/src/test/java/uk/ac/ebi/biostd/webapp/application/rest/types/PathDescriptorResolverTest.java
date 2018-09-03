package uk.ac.ebi.biostd.webapp.application.rest.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.biostd.webapp.application.rest.types.PathDescriptorResolver.MALFORMED_PATH_ERROR_MSG;

import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import uk.ac.ebi.biostd.webapp.application.rest.exceptions.ApiErrorException;

@RunWith(MockitoJUnitRunner.class)
public class PathDescriptorResolverTest {
    private static final String FILE_PATH = "folder/file.txt";
    private static final String ARCHIVE_PATH = "folder/archive.zip/";
    private static final String ARCHIVE_INNER_PATH = "archiveFolder/archiveFile.txt";
    private static final String HOST = "http://server:8888/biostd/files";
    private static final String MALFORMED_REQUEST = HOST + "/group/" + FILE_PATH;
    private static final String SIMPLE_USER_REQUEST = HOST + "/user/" + FILE_PATH;
    private static final String SIMPLE_GROUP_REQUEST = HOST + "/groups/group1/" + FILE_PATH;
    private static final String ARCHIVE_USER_REQUEST = HOST + "/user/" + ARCHIVE_PATH + ARCHIVE_INNER_PATH;

    @Mock
    private MethodParameter mockMethodParameter;

    @Mock
    private NativeWebRequest mockWebRequest;

    @Mock
    private HttpServletRequest mockRequest;

    private PathDescriptorResolver testInstance;

    @Before
    public void setUp() {
        testInstance = new PathDescriptorResolver();

        doReturn(PathDescriptor.class).when(mockMethodParameter).getParameterType();
    }

    @Test
    public void supportsParameter() {
        assertThat(testInstance.supportsParameter(mockMethodParameter)).isTrue();
    }

    @Test
    public void resolveSimpleUserRequest() {
        resolve(SIMPLE_USER_REQUEST, FILE_PATH, "");
    }

    @Test
    public void resolveSimpleGroupsRequest() {
        resolve(SIMPLE_GROUP_REQUEST, FILE_PATH, "");
    }

    @Test
    public void resolveArchiveRequest() {
        resolve(ARCHIVE_USER_REQUEST, ARCHIVE_PATH, ARCHIVE_INNER_PATH);
    }

    @Test
    public void resolveMalformedRequest() {
        assertThatExceptionOfType(ApiErrorException.class)
                .isThrownBy(() -> resolve(MALFORMED_REQUEST, "", ""))
                .withMessage(MALFORMED_PATH_ERROR_MSG);
    }

    private void resolve(String requestUrl, String expectedPath, String expectedArchivePath) {
        setRequestUrl(requestUrl);
        PathDescriptor pathDescriptor = (PathDescriptor)testInstance.resolveArgument(null, null, mockWebRequest, null);

        assertThat(pathDescriptor.getPath()).isEqualTo(expectedPath);
        assertThat(pathDescriptor.getArchivePath()).isEqualTo(expectedArchivePath);
    }

    private void setRequestUrl(String requestUrl) {
        when(mockWebRequest.getNativeRequest()).thenReturn(mockRequest);
        when(mockRequest.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
    }
}
