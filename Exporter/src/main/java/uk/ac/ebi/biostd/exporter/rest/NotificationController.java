package uk.ac.ebi.biostd.exporter.rest;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class NotificationController {

<<<<<<< Updated upstream
=======
    private final ExportPipeline fullExporter;
    private final ExportPipeline pmcExporter;
    private final PartialSubmissionExporter partialExporter;
    private final ExportPipeline statsExporter;

    public NotificationController(
            @Qualifier("full") ExportPipeline fullExporter,
            @Qualifier("pmc") ExportPipeline pmcExporter,
            @Qualifier("stats") ExportPipeline statsExporter,
            PartialSubmissionExporter partialExporter) {
        this.fullExporter = fullExporter;
        this.pmcExporter = pmcExporter;
        this.statsExporter = statsExporter;
        this.partialExporter = partialExporter;
    }

>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
=======

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

    @GetMapping("/api/force/pmc")
    public String pmcExport() {
        pmcExporter.execute();
        return "ok";
    }


    @GetMapping("/api/force/stats")
    public String statsExport() {
        statsExporter.execute();
        return "ok";
    }
>>>>>>> Stashed changes
}
