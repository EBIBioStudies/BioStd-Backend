package uk.ac.ebi.biostd.webapp.application.rest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListItemDto;

@Component
@Slf4j
@AllArgsConstructor
public class PendingSubmissionUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String ACCNO_PREFIX = "TMP_";

    private final ObjectMapper objectMapper;

    public String pendingSubmissionToString(PendingSubmissionDto dto) {
        return objectMapper.valueToTree(dto).toString();
    }

    public Optional<PendingSubmissionDto> pendingSubmissionFromString(String data) {
        try {
            return Optional.of(objectMapper.readValue(data, PendingSubmissionDto.class));
        } catch (IOException e) {
            log.error("error while parsing pending submission", e);
        }
        return Optional.empty();
    }

    public PendingSubmissionListItemDto pendingSubmissionToListItem(PendingSubmissionDto pendingSubmission) {
        PageTabProxy ptUtil = new PageTabProxy(pendingSubmission.getData());
        return PendingSubmissionListItemDto.builder()
                .accno(pendingSubmission.getAccno())
                .mtime(pendingSubmission.getModificationTimeInSeconds())
                .rtime(ptUtil.getReleaseDate().map(this::numberOfSeconds).orElse(0L))
                .title(ptUtil.getTitle().orElse(""))
                .build();
    }

    public PendingSubmissionDto createPendingSubmission(JsonNode pageTab) {
        Optional<String> accno = new PageTabProxy(pageTab).getAccno();

        PendingSubmissionDto submission = new PendingSubmissionDto();
        submission.setAccno(accno.orElse(newAccno()));
        submission.setData(pageTab);
        submission.setChanged(System.currentTimeMillis());
        return submission;
    }

    public boolean isTemporaryAccno(String accno) {
        return accno.startsWith(ACCNO_PREFIX);
    }

    private String newAccno() {
        return ACCNO_PREFIX + System.currentTimeMillis();
    }

    private Long numberOfSeconds(String date) {
        return LocalDate.parse(date, DATE_FORMAT).atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }
}
