package uk.ac.ebi.biostd.exporter.rest;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.biostd.exporter.jobs.full.FullSubmissionExporter;
import uk.ac.ebi.biostd.exporter.jobs.partial.PartialSubmissionExporter;

@Slf4j
@RestController
@AllArgsConstructor
public class NotificationController {

    private final FullSubmissionExporter fullExporter;
    private final PartialSubmissionExporter partialExporter;

    @GetMapping("/api/update/partial")
    public String partialUpdate() {
        log.info("received partial update notification at {}", Instant.now());
        return "ok";
    }

    @GetMapping("/api/update/full")
    public String fullUpdate() {
        log.info("received full update notification at {}", Instant.now());
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
