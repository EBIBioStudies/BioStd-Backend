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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Import(WebConfiguration.class)
public class PendingSubmissionUtilTest {

    private static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final long MILLISECONDS = 1000L;
    private static final long SECONDS = MILLISECONDS / 1000L;

    private static final String PAGETAB_JSON = "12345.pagetab.json";
    private static final String ACCNO = "12345";
    private static final String TITLE = "PageTab data";
    private static final LocalDate RELEASE_DATE = LocalDate.of(2018, 5, 21);

    private static final Map<String, String> PAGETAB_JSON_PARAMS = Collections.unmodifiableMap(
            new HashMap<String, String>() {
                {
                    put("ACCNO", ACCNO);
                    put("TITLE", TITLE);
                    put("RELEASE_DATE", RELEASE_DATE.format(DATE_FORMATTER));
                }
            }
    );

    @Autowired
    private ObjectMapper objectMapper;

    private PendingSubmissionUtil util;

    @Before
    public void setUp() {
        util = new PendingSubmissionUtil(objectMapper);
    }

    @Test
    public void testParseInvalidData() {
        Optional<PendingSubmissionDto> dto = util.parse("invalid data");
        assertThat(dto.isPresent()).isFalse();
    }

    @Test
    public void testParseValidData() throws IOException {
        final JsonNode pageTab = getPageTab();

        Optional<PendingSubmissionDto> dto = util.parse(pendingSubmissionAsString(ACCNO, pageTab));
        assertThat(dto.isPresent()).isTrue();
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
        assertThat(listItem.getTitle()).isEqualTo(TITLE);
        assertThat(listItem.getRtime()).isEqualTo(seconds(RELEASE_DATE));
    }

    @Test
    public void testCreateFromExisted() throws IOException {
        final String pageTab = getPageTab().toString();

        long from = System.currentTimeMillis();
        Optional<PendingSubmissionDto> dto = util.create(pageTab);
        long to = System.currentTimeMillis();

        assertThat(dto.isPresent()).isTrue();
        dto.ifPresent(v -> {
            assertThat(v.getAccno()).isEqualTo(ACCNO);
            assertThat(v.getChanged()).isBetween(from, to);
            assertThat(v.getData().toString()).isEqualTo(pageTab);
        });
    }

    @Test
    public void testCreateFromEmpty() {
        final String pageTab = "{}";

        long from = System.currentTimeMillis();
        Optional<PendingSubmissionDto> dto = util.create(pageTab);
        long to = System.currentTimeMillis();

        assertThat(dto.isPresent()).isTrue();
        dto.ifPresent(v -> {
            assertThat(v.getAccno()).matches("^TMP_.*");
            assertThat(v.getChanged()).isBetween(from, to);
            assertThat(v.getData().toString()).isEqualTo(pageTab);
        });
    }

    @Test
    public void testCreateFromInvalidData() {
        Optional<PendingSubmissionDto> dto = util.create("invalid data");
        assertThat(dto.isPresent()).isFalse();
    }

    private String pendingSubmissionAsString(String accno, JsonNode pageTab) throws JsonProcessingException {
        return objectMapper.writeValueAsString(createPendingSubmission(accno, pageTab));
    }

    private JsonNode getPageTab() throws IOException {
        String template = ResourceHandler.readFile(PendingSubmissionUtilTest.class.getResource(PAGETAB_JSON).getPath());
        return objectMapper.readTree(replace(template, PAGETAB_JSON_PARAMS));
    }

    private static PendingSubmissionDto createPendingSubmission(String accno, JsonNode pageTab) {
        PendingSubmissionDto dto = new PendingSubmissionDto();
        dto.setAccno(accno);
        dto.setData(pageTab);
        dto.setChanged(MILLISECONDS);
        return dto;
    }

    private static long seconds(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    private static String replace(String template, Map<String, String> valueMap) {
        final String[] accum = new String[]{template};
        valueMap.forEach((key, value) -> accum[0] = accum[0].replaceAll(format("\\$\\{%s\\}", key), value));
        return accum[0];
    }
}

