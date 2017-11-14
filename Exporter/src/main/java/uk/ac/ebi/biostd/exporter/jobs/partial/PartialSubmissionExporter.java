package uk.ac.ebi.biostd.exporter.jobs.partial;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biostd.exporter.model.Submission;
import uk.ac.ebi.biostd.exporter.service.SubmissionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartialSubmissionExporter {

    private final PartialExportJobProperties configProperties;
    private final ObjectMapper objectMapper;
    private final SubmissionService submissionService;
    private final RestTemplate restTemplate;

    private long lastSyncTime = getBeginOfTheDateEpoch();

    public void execute() {
        log.info("executing partial export file job at {}", Instant.now());
        List<Submission> submissions = submissionService.getUpdatedSubmissions(lastSyncTime);

        if (submissions.size() > 0) {
            writeFile(submissions);
            notifyFrontend();
        }

        lastSyncTime = getNowEpoch();
        log.info("finish processing updated submissions");
    }

    public void executeSingle(long id) {
        Submission submission = submissionService.getSubmission(id);
        writeFile(Collections.singletonList(submission));
        notifyFrontend();
    }

    @SneakyThrows
    private void notifyFrontend() {
        restTemplate.getForEntity(configProperties.getNotificationUrl(), String.class);
    }

    @SneakyThrows
    private void writeFile(List<Submission> submissions) {
        String fullFilePath = configProperties.getFilePath() + configProperties.getFileName();
        Files.deleteIfExists(Paths.get(fullFilePath));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fullFilePath))) {
            PartialUpdateFile updateFile = PartialUpdateFile.builder()
                    .submissions(submissions)
                    .submissionsCount(submissions.size())
                    .updatedSubmissions(submissions.stream().map(Submission::getAccno).collect(toList()))
                    .build();

            bw.write(objectMapper.writeValueAsString(updateFile));
        }
    }

    private long getBeginOfTheDateEpoch() {
        return ZonedDateTime.now()
                .truncatedTo(ChronoUnit.DAYS).toLocalDate()
                .atStartOfDay(ZoneId.of("Europe/London")).toEpochSecond();
    }

    private long getNowEpoch() {
        return ZonedDateTime.now(ZoneId.of("Europe/London")).toEpochSecond();
    }
}
