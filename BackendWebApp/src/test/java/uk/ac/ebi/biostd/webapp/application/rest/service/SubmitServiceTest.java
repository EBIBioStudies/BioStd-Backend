package uk.ac.ebi.biostd.webapp.application.rest.service;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.biostd.webapp.application.rest.service.SubmitService.DEFAULT_ACCNO_TEMPLATE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pri.util.collection.Collections;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.model.SubmissionAttribute;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.SimpleLogNode;
import uk.ac.ebi.biostd.treelog.SubmissionReport;
import uk.ac.ebi.biostd.util.DataFormat;
import uk.ac.ebi.biostd.webapp.application.configuration.WebConfiguration;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitOperation;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitReportDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitStatus;
import uk.ac.ebi.biostd.webapp.server.mng.SubmissionManager;
import uk.ac.ebi.biostd.webapp.server.mng.impl.JPASubmissionManager;

@RunWith(SpringRunner.class)
@Import(WebConfiguration.class)
public class SubmitServiceTest {

    @Autowired private ObjectMapper objectMapper;
    @MockBean private JPASubmissionManager submissionManager;
    private SubmitService submitService;
    private User user = new User();

    @Before
    public void setUp() {
        submitService = new SubmitService(submissionManager, objectMapper);
    }

    @Test
    public void testEmptyFile() {
        MockMultipartFile testFile = new MockMultipartFile("file", "study.json", "application/json", new byte[]{});
        SubmitReportDto dto = submitService.submit(testFile, Collections.emptySet(), null, SubmitOperation.CREATE, user);
        assertThat(dto.getStatus(), equalTo(SubmitStatus.FAIL));
        assertThat(dto.getLog().getMessage(), is(notNullValue()));
    }

    @Test
    public void testJSONSubmit() throws IOException {
        MockMultipartFile testFile = new MockMultipartFile("file", "study.json", "application/json", "{}".getBytes());

        String result = createSubmission(testFile, SubmitOperation.CREATE, Collections.emptyMap(), null);

        assertThat(result, isJson());
        assertThat(result, hasJsonPath("$.submissions"));
        assertThat(result, hasJsonPath("$.submissions[0].accno", equalTo(DEFAULT_ACCNO_TEMPLATE)));
    }

    @Test
    public void testXLSXSubmit() throws IOException {
        byte[] bytes = Files.readAllBytes(new File(PendingSubmissionUtilTest.class.getResource("test.xlsx").getPath()).toPath());
        MockMultipartFile testFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);

        String result = createSubmission(testFile, SubmitOperation.CREATE_OR_UPDATE, Collections.emptyMap(), null);

        assertThat(result, isJson());
        assertThat(result, hasJsonPath("$.submissions"));
        assertThat(result, hasJsonPath("$.submissions[0].accno", equalTo("TEST_12345")));
    }

    @Test
    public void testAccnoTemplate() throws IOException {
        String accnoTemplate = "!{A-BCD-}";
        MockMultipartFile testFile = new MockMultipartFile("file", "study.json", "application/json", "{}".getBytes());

        String result = createSubmission(testFile, SubmitOperation.CREATE, Collections.emptyMap(), accnoTemplate);

        assertThat(result, isJson());
        assertThat(result, hasJsonPath("$.submissions"));
        assertThat(result, hasJsonPath("$.submissions[0].accno", equalTo(accnoTemplate)));
    }

    @Test
    public void testSingleProjectAccno() throws IOException {
        String template = "!{A-BCD-}";
        Map<String, String> projectAccessions = new HashMap<>();
        projectAccessions.put("Parent-1", template);

        MockMultipartFile testFile = new MockMultipartFile("file", "study.json", "application/json", "{}".getBytes());

        String result = createSubmission(testFile, SubmitOperation.CREATE, projectAccessions, null);

        assertThat(result, isJson());
        assertThat(result, hasJsonPath("$.submissions"));
        assertThat(result, hasJsonPath("$.submissions[0].accno", equalTo(template)));
        assertThat(result, hasJsonPath("$.submissions[0].attributes[?(@.name == 'AttachTo')].value", hasItem("Parent-1")));
    }

    @Test
    public void testMultipleProjectAccno() throws IOException {
        Map<String, String> projectAccessions = new HashMap<>();
        projectAccessions.put("Parent-1", "!{AAA}");
        projectAccessions.put("Parent-2", "!{BBB}");

        MockMultipartFile testFile = new MockMultipartFile("file", "study.json", "application/json", "{}".getBytes());

        String result = createSubmission(testFile, SubmitOperation.CREATE, projectAccessions, null);

        assertThat(result, isJson());
        assertThat(result, hasJsonPath("$.submissions"));
        assertThat(result, hasJsonPath("$.submissions[0].accno", equalTo(DEFAULT_ACCNO_TEMPLATE)));
        assertThat(result, hasJsonPath("$.submissions[0].attributes[?(@.name == 'AttachTo')].value", hasItems("Parent-1", "Parent-2")));
    }

    private String createSubmission(MultipartFile testFile, SubmitOperation operation,
            Map<String, String> projectAccNumbers, String accnoTemplate) throws IOException {

        SubmissionReport submissionReport = new SubmissionReport();
        submissionReport.setLog(new SimpleLogNode(LogNode.Level.SUCCESS, "success", null));
        when(submissionManager.createSubmission(any(byte[].class), eq(DataFormat.json), eq("UTF-8"),
                eq(operation.toLegacyOp()), eq(user), eq(false), eq(false), any()))
                .thenReturn(submissionReport);

        when(submissionManager.getSubmissionsByAccession(any(String.class)))
                .thenAnswer((Answer<Submission>) invocation -> {
                    String arg = invocation.getArgument(0);
                    return projectAccNumbers.containsKey(arg) ? submissionMock(projectAccNumbers.get(arg)) : null;
                });

        submitService.submit(testFile, projectAccNumbers.keySet(), accnoTemplate, operation, user);

        ArgumentCaptor<byte[]> argCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(submissionManager).createSubmission(argCaptor.capture(), eq(DataFormat.json), eq("UTF-8"),
                any(SubmissionManager.Operation.class), any(User.class), anyBoolean(), anyBoolean(), any());

        return new String(argCaptor.getValue(), "UTF-8");
    }

    private Submission submissionMock(String accnoTemplate) {
        final Optional<String> tmpl = Optional.ofNullable(accnoTemplate);

        Submission subm = Mockito.mock(Submission.class);
        when(subm.getAttributes())
                .thenReturn(
                        tmpl.map(Stream::of).orElseGet(Stream::empty)
                                .map(v -> new SubmissionAttribute("accnoTemplate", v))
                                .collect(toList()));
        return subm;
    }
}
