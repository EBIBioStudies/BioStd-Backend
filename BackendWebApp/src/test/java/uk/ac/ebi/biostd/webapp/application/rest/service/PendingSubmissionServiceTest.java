
package uk.ac.ebi.biostd.webapp.application.rest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.configuration.WebConfiguration;
import uk.ac.ebi.biostd.webapp.application.domain.services.UserDataService;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserData;
import uk.ac.ebi.biostd.webapp.application.rest.dto.*;

@RunWith(SpringRunner.class)
@Import(WebConfiguration.class)
public class PendingSubmissionServiceTest {

    private static final String TOPIC = "submission";

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserDataService userDataService;

    @MockBean
    private SubmitService submitService;

    private PendingSubmissionService pendingSubmissionService;

    @Before
    public void setUp() {
        pendingSubmissionService =
                new PendingSubmissionService(userDataService, new PendingSubmissionUtil(objectMapper), submitService);
    }

    @Test
    public void testGetSubmissionList() throws JsonProcessingException {
        final User user = newUser();

        when(userDataService.findAllByUserAndTopic(user.getId(), TOPIC))
                .thenReturn(ImmutableList.of(
                        newUserData("1"),
                        newUserData("2")
                ));

        PendingSubmissionListDto result = pendingSubmissionService.getSubmissionList(new PendingSubmissionListFiltersDto(), user);

        assertThat(result.getSubmissions().size()).isEqualTo(2);
        assertThat(result.getSubmissions().get(0).getAccno()).isEqualTo("1");
        assertThat(result.getSubmissions().get(1).getAccno()).isEqualTo("2");
    }

    @Test
    public void testDeleteSubmission() {
        final User user = newUser();
        final String accno = "1234";

        doNothing().when(userDataService).deleteModifiedSubmission(user.getId(), accno);
        pendingSubmissionService.deleteSubmissionByAccNo(accno, user);

        verify(userDataService, times(1)).deleteModifiedSubmission(user.getId(), accno);
    }

    @Test
    public void testUpdateSubmission() throws JsonProcessingException {
        final User user = newUser();
        final PendingSubmissionDto dto = newPendingSubmissionDto("1234");
        final UserData userData = newUserData(dto);

        when(userDataService.findByUserAndKey(user.getId(), dto.getAccno()))
                .thenReturn(Optional.of(userData));

        when(userDataService.update(eq(user.getId()), eq(dto.getAccno()), any(String.class), eq(TOPIC)))
                .thenAnswer((Answer<UserData>) invocation -> {
                    UserData ud = new UserData();
                    ud.setData(invocation.getArgument(2));
                    return ud;
                });

        Optional<PendingSubmissionDto> optResult = pendingSubmissionService.updateSubmission(dto.getAccno(), dto.getData(), user);
        assertThat(optResult.isPresent()).isTrue();

        PendingSubmissionDto result = optResult.get();
        assertThat(result.getChanged()).isGreaterThan(dto.getChanged());
        assertThat(result.getData().toString()).isEqualTo(dto.getData().toString());
        assertThat(result.getAccno()).isEqualTo(dto.getAccno());
    }

    @Test
    public void testCreateNewSubmission() {
        final User user = newUser();
        final JsonNode json = objectMapper.createObjectNode();

        when(userDataService.update(eq(user.getId()), any(String.class), any(String.class), eq(TOPIC)))
                .thenAnswer((Answer<UserData>) invocation -> {
                    UserData ud = new UserData();
                    ud.setData(invocation.getArgument(2));
                    return ud;
                });

        PendingSubmissionDto result = pendingSubmissionService.createSubmission(json, user);
        assertThat(result.getAccno()).matches("TMP_.+");
        assertThat(result.getChanged()).isGreaterThan(0);
        assertThat(result.getData().toString()).isEqualTo(json.toString());
    }

    @Test
    public void testCreateCopySubmission() {
        final User user = newUser();
        final String accno = "1234";
        final ObjectNode json = objectMapper.createObjectNode();
        json.put("accno", accno);

        when(userDataService.update(eq(user.getId()), any(String.class), any(String.class), eq(TOPIC)))
                .thenAnswer((Answer<UserData>) invocation -> {
                    UserData ud = new UserData();
                    ud.setData(invocation.getArgument(2));
                    return ud;
                });

        PendingSubmissionDto result = pendingSubmissionService.createSubmission(json, user);
        assertThat(result.getAccno()).isEqualTo(accno);
        assertThat(result.getChanged()).isGreaterThan(0);
        assertThat(result.getData().toString()).isEqualTo(json.toString());
    }

    @Test
    public void testSubmitNewSubmission() throws JsonProcessingException {
        testSubmit("TMP_1234", SubmitOperation.CREATE);
    }

    @Test
    public void testSubmitCopySubmission() throws JsonProcessingException {
        testSubmit("1234", SubmitOperation.CREATE_OR_UPDATE);
    }

    private void testSubmit(String accno, SubmitOperation op) throws JsonProcessingException {
        final User user = newUser();

        when(userDataService.findByUserAndKey(user.getId(), accno))
                .thenReturn(Optional.of(newUserData(accno)));

        when(submitService.submitJson(any(JsonNode.class), eq(op), eq(user)))
                .thenReturn(SubmitReportDto.builder().status(SubmitStatus.OK).build());

        Optional<SubmitReportDto> optResult = pendingSubmissionService.submitSubmission(accno, user);
        assertThat(optResult.isPresent()).isTrue();

        SubmitReportDto result = optResult.get();
        assertThat(result.getStatus()).isEqualTo(SubmitStatus.OK);
    }

    private User newUser() {
        User user = new User();
        user.setId(1);
        return user;
    }

    private PendingSubmissionDto newPendingSubmissionDto(String accno) {
        final PendingSubmissionDto dto = new PendingSubmissionDto();
        dto.setAccno(accno);
        dto.setChanged(1L);
        dto.setData(objectMapper.createObjectNode());
        return dto;
    }

    private UserData newUserData(String accno) throws JsonProcessingException {
        return newUserData(newPendingSubmissionDto(accno));
    }

    private UserData newUserData(PendingSubmissionDto dto) throws JsonProcessingException {
        UserData userData = new UserData();
        userData.setData(objectMapper.writeValueAsString(dto));
        return userData;
    }
}

