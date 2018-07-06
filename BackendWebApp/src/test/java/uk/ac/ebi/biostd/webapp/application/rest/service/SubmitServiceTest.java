package uk.ac.ebi.biostd.webapp.application.rest.service;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.pri.util.collection.Collections;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.model.SubmissionAttribute;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.SimpleLogNode;
import uk.ac.ebi.biostd.treelog.SubmissionReport;
import uk.ac.ebi.biostd.util.DataFormat;
import uk.ac.ebi.biostd.webapp.application.configuration.WebConfiguration;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitOperation;
import uk.ac.ebi.biostd.webapp.server.mng.SubmissionManager;
import uk.ac.ebi.biostd.webapp.server.mng.impl.JPASubmissionManager;

@RunWith(SpringRunner.class)
@Import(WebConfiguration.class)
public class SubmitServiceTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JPASubmissionManager submissionManager;

    private SubmitService submitService;

    private User user = new User();

    @Before
    public void setUp() {
        submitService = new SubmitService(submissionManager, objectMapper);
    }

    @Test
    public void testEmptyFile() {
       //TODO:  submitService.createOrUpdateSubmission();
    }

    @Test
    public void testJsonSubmit() throws IOException {
        Set<String> projectAccNumbers = Collections.emptySet();

        createMocks(DataFormat.json, "UTF-8", SubmissionManager.Operation.CREATE, user, projectAccNumbers, "!{PROJECT-}");

        MockMultipartFile testFile = new MockMultipartFile("file", "study.json", "application/json", "{}".getBytes());
        submitService.createOrUpdateSubmission(testFile, projectAccNumbers, null, SubmitOperation.CREATE, user);

        ArgumentCaptor<byte[]> argCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(submissionManager).createSubmission(argCaptor.capture(), any(DataFormat.class), any(String.class),
                any(SubmissionManager.Operation.class), any(User.class), anyBoolean(), anyBoolean(), any(String.class));

        JsonNode result = objectMapper.readTree(argCaptor.getValue());
        //TODO verify result

    }

    private void createMocks(DataFormat dataFormat, String charset, SubmissionManager.Operation operation, User user, Set<String> accNumbers, String accnoTemplate) {
        SubmissionReport submissionReport = new SubmissionReport();
        submissionReport.setLog(new SimpleLogNode(LogNode.Level.SUCCESS, "success", null));
        when(submissionManager.createSubmission(any(byte[].class), eq(dataFormat), eq(charset), eq(operation), eq(user), eq(false), eq(false), any(String.class)))
                .thenReturn(submissionReport);

        when(submissionManager.getSubmissionsByAccession(any(String.class)))
                .thenAnswer(new Answer<Submission>() {
                    @Override
                    public Submission answer(InvocationOnMock invocation) throws Throwable {
                        String arg = invocation.getArgument(0);
                        return accNumbers.contains(arg) ? submissionMock(accnoTemplate) : null;
                    }
                });
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
