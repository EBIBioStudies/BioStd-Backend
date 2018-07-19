package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.domain.services.SubmissionDataService;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitOperation;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitReportDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitStatus;
import uk.ac.ebi.biostd.webapp.application.rest.mappers.SubmissionsMapper;
import uk.ac.ebi.biostd.webapp.application.rest.service.SubmitService;

@RunWith(SpringRunner.class)
@WebMvcTest(SubmissionResource.class)
@AutoConfigureMockMvc(secure = false, addFilters = false)
public class SubmissionResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SubmitService submitService;

    @MockBean
    private SubmissionDataService submissionService;

    @MockBean
    private SubmissionsMapper submissionsMapper;

    private User user = new User();

    @Before
    public void onSetUp() {
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testFileSubmit() throws Exception {
        final String accnoTemplate = "!{TEST-}";
        final String[] attachTo = new String[]{"1", "2"};

        MockMultipartFile testFile = new MockMultipartFile("file", "study.json", "application/json", "{}".getBytes());

        when(submitService.submit(testFile, new HashSet<>(Arrays.asList(attachTo)), accnoTemplate, SubmitOperation.CREATE, user))
                .thenReturn(SubmitReportDto.builder().status(SubmitStatus.OK).build());

        mvc.perform(multipart("/submissions/file_submit/create")
                .file(testFile)
                .param("attachTo", attachTo[0])
                .param("attachTo", attachTo[1])
                .param("accnoTemplate", accnoTemplate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.status", is("OK")));
    }

    @Test
    public void testFileSubmitNoParams() throws Exception {
        MockMultipartFile testFile = new MockMultipartFile("file", "study.json", "application/json", "{}".getBytes());

        when(submitService.submit(eq(testFile), anySet(), isNull(), eq(SubmitOperation.CREATE), eq(user)))
                .thenReturn(SubmitReportDto.builder().status(SubmitStatus.OK).build());

        mvc.perform(multipart("/submissions/file_submit/create")
                .file(testFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.status", is("OK")));
    }
}
