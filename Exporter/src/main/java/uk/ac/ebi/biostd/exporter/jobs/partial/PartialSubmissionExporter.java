package uk.ac.ebi.biostd.exporter.jobs.partial;

import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biostd.exporter.model.Submission;
import uk.ac.ebi.biostd.exporter.service.SubmissionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartialSubmissionExporter {

    private static final String FILE_FORMAT = "%s%s_%s.json";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");

    private final PartialExportJobProperties configProperties;
    private final ObjectMapper objectMapper;
    private final SubmissionService submissionService;
    private final RestTemplate restTemplate;

    private long lastSyncTime = getBeginOfTheDateEpoch();

    public void execute() {
        log.info("executing partial export file job at {}", Instant.now(Clock.systemUTC()));

        processCreatedAndUpdatedSubmissions();
        processDeletedSubmissions();

        lastSyncTime = getNowEpoch();
        log.info("finish processing updated/deleting submissions");
    }

    private void processDeletedSubmissions() {
        submissionService.getDeletedSubmissions(lastSyncTime).forEach(this::notifyDeletion);
    }

    private void notifyDeletion(String submissionAccno) {
        String url = configProperties.getDeleteNotificationUrl() + submissionAccno;
        log.info("notifying deleted submission to frontend at {}", url);
        restTemplate.getForEntity(url, String.class);
    }

    private void processCreatedAndUpdatedSubmissions() {
        List<Submission> submissions = submissionService.getUpdatedSubmissions(lastSyncTime);

        if (submissions.size() > 0) {
            String fileName = writeFile(submissions);
            notifyFrontend(fileName);
        }
    }

    @SneakyThrows
    private void notifyFrontend(String fileName) {
        String url = configProperties.getNotificationUrl() + fileName;
        log.info("notify frontend at {}", url);
        restTemplate.getForEntity(url, String.class);
    }

    @SneakyThrows
    private String writeFile(List<Submission> submissions) {
        String fullFilePath = getFileName();
        Files.deleteIfExists(Paths.get(fullFilePath));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fullFilePath))) {
            PartialUpdateFile updateFile = PartialUpdateFile.builder()
                    .submissions(submissions)
                    .submissionsCount(submissions.size())
                    .updatedSubmissions(submissions.stream().map(Submission::getAccno).collect(toList()))
                    .build();

            bw.write(objectMapper.writeValueAsString(updateFile));
        }

        return FilenameUtils.getName(fullFilePath);
    }

    private long getBeginOfTheDateEpoch() {
        return ZonedDateTime.now()
                .truncatedTo(ChronoUnit.DAYS).toLocalDate()
                .atStartOfDay(ZoneId.of("Europe/London")).toEpochSecond();
    }

    private long getNowEpoch() {
        return ZonedDateTime.now(ZoneId.of("Europe/London")).toEpochSecond();
    }

    private String getFileName() {
        String timestamp = OffsetDateTime.now(UTC).format(formatter);
        return String.format(FILE_FORMAT, configProperties.getFilePath(), configProperties.getFileName(), timestamp);
    }
}
