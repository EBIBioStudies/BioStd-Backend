package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.core.Is.is;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListFiltersDto;
import uk.ac.ebi.biostd.webapp.application.rest.service.PendingSubmissionService;
import uk.ac.ebi.biostd.webapp.application.rest.service.PendingSubmissionUtil;


@RunWith(SpringRunner.class)
@WebMvcTest(PendingSubmissionResource.class)
@AutoConfigureMockMvc(secure = false)
public class PendingSubmissionResourceTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PendingSubmissionService pendingSubmissionService;

    private User user = new User();
    private PendingSubmissionUtil util = new PendingSubmissionUtil(objectMapper);

    @Before
    public void onSetUp() {
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testGetSubmissionList() throws Exception {
        PendingSubmissionListFiltersDto filters = new PendingSubmissionListFiltersDto();

        when(pendingSubmissionService.getSubmissionList(filters, user))
                .thenReturn(new PendingSubmissionListDto(
                        ImmutableList.of(
                                newPendingSubmission("1"),
                                newPendingSubmission("2"),
                                newPendingSubmission("3")
                        ).stream().map(util::pendingSubmissionToListItem).collect(Collectors.toList())));

        //TODO: add filter params
        mvc.perform(get("/submissions/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submissions").exists());
    }

    @Test
    public void testGetSubmission() throws Exception {
        PendingSubmissionDto dto = newPendingSubmission();

        when(pendingSubmissionService.getSubmissionByAccNo(dto.getAccno(), user))
                .thenReturn(Optional.of(dto));

        mvc.perform(get("/submissions/pending/" + dto.getAccno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accno", is(dto.getAccno())))
                .andExpect(jsonPath("$.changed", is((int) dto.getChanged())))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    public void testGetSubmissionBadRequest() throws Exception {
        String accno = "1234";

        when(pendingSubmissionService.getSubmissionByAccNo(accno, user))
                .thenReturn(Optional.empty());

        mvc.perform(get("/submissions/pending/" + accno))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateSubmission() throws Exception {
        PendingSubmissionDto dto = newPendingSubmission();

        when(pendingSubmissionService.createSubmission(dto.getData(), user))
                .thenReturn(dto);

        mvc.perform(post("/submissions/pending")
                .content(dto.getData().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accno", is(dto.getAccno())))
                .andExpect(jsonPath("$.changed", is((int) dto.getChanged())))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    public void testCreateSubmissionBadRequest() throws Exception {
        mvc.perform(post("/submissions/pending")
                .content("not a json data")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private PendingSubmissionDto newPendingSubmission() {
        return newPendingSubmission("12345");
    }

    private PendingSubmissionDto newPendingSubmission(String accno) {
        JsonNode data = objectMapper.createObjectNode();

        PendingSubmissionDto dto = new PendingSubmissionDto();
        dto.setAccno(accno);
        dto.setChanged(1L);
        dto.setData(data);
        return dto;
    }
}
