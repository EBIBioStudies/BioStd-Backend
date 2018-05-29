package uk.ac.ebi.biostd.webapp.application.rest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.backend.testing.ResourceHandler;
import uk.ac.ebi.biostd.webapp.application.configuration.WebConfiguration;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListItemDto;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Import(WebConfiguration.class)
public class PendingSubmissionUtilTest {

    private static final String ACCNO = "12345";
    private static final String MINIMAL_PAGETAB = "12345.pagetab.json";
    private static final long MILLISECONDS = 1000L;
    private static final long SECONDS = MILLISECONDS / 1000L;

    @Autowired
    private ObjectMapper objectMapper;

    private PendingSubmissionUtil util;

    @Before
    public void setUp() {
        util = new PendingSubmissionUtil(objectMapper);
    }

    @Test
    public void testParseInvalidData() {
        final String invalidData = "blah blah";
        Optional<PendingSubmissionDto> dto = util.parse(invalidData);
        assertThat(dto.isPresent()).isEqualTo(false);
    }

    @Test
    public void testParseValidData() throws IOException {
        final JsonNode pageTab = getPageTab();

        Optional<PendingSubmissionDto> dto = util.parse(pendingSubmissionAsString(ACCNO, pageTab));
        assertThat(dto.isPresent()).isEqualTo(true);
        dto.ifPresent(v -> {
            assertThat(v.getAccno()).isEqualTo(ACCNO);
            assertThat(v.getChanged()).isEqualTo(MILLISECONDS);
            assertThat(v.getData().toString()).isEqualTo(pageTab.toString());
        });
    }

    @Test
    public void testConvertInvalidData() {
        final JsonNode pageTab = objectMapper.createObjectNode();

        PendingSubmissionListItemDto listItem = util.convert(createPendingSubmission(ACCNO, pageTab));
        assertThat(listItem.getAccno()).isEqualTo(ACCNO);
        assertThat(listItem.getTitle()).isEmpty();
        assertThat(listItem.getRtime()).isNull();
        assertThat(listItem.getMtime()).isEqualTo(SECONDS);
    }

    @Test
    public void testConvertValidData() throws IOException {
        final JsonNode pageTab = getPageTab();

        PendingSubmissionListItemDto listItem = util.convert(createPendingSubmission(ACCNO, pageTab));
        assertThat(listItem.getAccno()).isEqualTo(ACCNO);
        assertThat(listItem.getMtime()).isEqualTo(SECONDS);
        assertThat(listItem.getTitle()).isEqualTo("Minimal page tab data");
        assertThat(listItem.getRtime()).isEqualTo(seconds(2018, 5, 21));
    }

/*
    @Test
    public void testCreate() {

    }
*/

    private String pendingSubmissionAsString(String accno, JsonNode pageTab) throws JsonProcessingException {
        return objectMapper.writeValueAsString(createPendingSubmission(accno, pageTab));
    }

    private JsonNode getPageTab() throws IOException {
        return objectMapper.readTree(ResourceHandler
                .readFile(PendingSubmissionUtilTest.class.getResource(MINIMAL_PAGETAB).getPath()));
    }

    private static PendingSubmissionDto createPendingSubmission(String accno, JsonNode pageTab) {
        PendingSubmissionDto dto = new PendingSubmissionDto();
        dto.setAccno(accno);
        dto.setData(pageTab);
        dto.setChanged(MILLISECONDS);
        return dto;
    }

    private static long seconds(int year, int month, int day) {
        return LocalDate.of(year, month, day).atStartOfDay(ZoneOffset.UTC).toInstant().getEpochSecond();
    }

}

