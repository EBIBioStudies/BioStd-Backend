package uk.ac.ebi.biostd.exporter.rest;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.biostd.exporter.jobs.full.FullSubmissionExporter;
import uk.ac.ebi.biostd.exporter.jobs.partial.PartialSubmissionExporter;

@Slf4j
@RestController
@AllArgsConstructor
public class NotificationController {

    private final FullSubmissionExporter fullExporter;
    private final PartialSubmissionExporter partialExporter;

    @GetMapping("/api/update/partial/{fileName}")
    public String partialUpdate(@PathVariable(name = "fileName") String fileName) {
        log.info("received partial update notification at {} with file {}", Instant.now(), fileName);
        return "ok";
    }

    @GetMapping("/api/update/full/{fileName}")
    public String fullUpdate(@PathVariable(name = "fileName") String fileName) {
        log.info("received full update notification at {} with file {}", Instant.now(), fileName);
        return "ok";
    }


    @GetMapping("/api/force/full")
    public String forceFull() {
        fullExporter.execute();
        return "ok";
    }

    @GetMapping("/api/force/partial")
    public String forcePartial() {
        partialExporter.execute();
        return "ok";
    }
}
