package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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
        final PendingSubmissionListFiltersDto filters = new PendingSubmissionListFiltersDto();
        filters.setAccNo("ABC");
        filters.setKeywords("test");
        filters.setOffset(1);
        filters.setLimit(15);
        filters.setRTimeFrom(2L);
        filters.setRTimeTo(3L);

        PendingSubmissionListDto listDto = new PendingSubmissionListDto(
                ImmutableList.of(
                        newPendingSubmission("1"),
                        newPendingSubmission("2"),
                        newPendingSubmission("3")
                ).stream().map(util::pendingSubmissionToListItem).collect(Collectors.toList()));

        when(pendingSubmissionService.getSubmissionList(any(PendingSubmissionListFiltersDto.class), eq(user)))
                .thenAnswer(new Answer<PendingSubmissionListDto>() {
                    @Override
                    public PendingSubmissionListDto answer(InvocationOnMock invocation) throws Throwable {
                        PendingSubmissionListFiltersDto filtersArg = invocation.getArgument(0);
                        return isEqual(filters, filtersArg) ? listDto : new PendingSubmissionListDto();
                    }

                    private boolean isEqual(PendingSubmissionListFiltersDto o1, PendingSubmissionListFiltersDto o2) {
                        return Objects.equals(o1.getOffset(), o2.getOffset()) &&
                                Objects.equals(o1.getLimit(), o2.getLimit()) &&
                                Objects.equals(o1.getAccNo(), o2.getAccNo()) &&
                                Objects.equals(o1.getRTimeFrom(), o2.getRTimeFrom()) &&
                                Objects.equals(o1.getRTimeTo(), o2.getRTimeTo()) &&
                                Objects.equals(o1.getKeywords(), o2.getKeywords());
                    }
                });

        mvc.perform(get("/submissions/pending")
                .param("offset", filters.getOffset() + "")
                .param("limit", filters.getLimit() + "")
                .param("accNo", filters.getAccNo().orElse(""))
                .param("rTimeFrom", filters.getRTimeFrom().orElse(1L) + "")
                .param("rTimeTo", filters.getRTimeTo().orElse(1L) + "")
                .param("keywords", filters.getKeywords().orElse("")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submissions").exists())
                .andExpect(jsonPath("$.submissions", hasSize(3)));
    }

    @Test
    public void testGetSubmissionListBadRequest() throws Exception {
        mvc.perform(get("/submissions/pending")
                .param("offset", "not a number")
                .param("limit", "not a number"))
                .andExpect(status().isBadRequest());
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

    @Test
    public void testUpdateSubmission() throws Exception {
        final String accno = "1234";
        when(pendingSubmissionService.updateSubmission(eq(accno), any(JsonNode.class), eq(user)))
                .thenReturn(Optional.of(new PendingSubmissionDto()));

        mvc.perform(put("/submissions/pending/" + accno)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateSubmissionBadRequest() throws Exception {
        final String accno = "1234";
        when(pendingSubmissionService.updateSubmission(eq(accno), any(JsonNode.class), eq(user)))
                .thenReturn(Optional.empty());

        mvc.perform(put("/submissions/pending/" + accno)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteSubmission() throws Exception {
        final String accno = "1234";
        doNothing().when(pendingSubmissionService).deleteSubmissionByAccNo(accno, user);

        mvc.perform(delete("/submissions/pending/" + accno))
                .andExpect(status().isOk());
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
