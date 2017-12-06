package uk.ac.ebi.biostd.exporter.schedulers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.full.FullSubmissionExporter;
import uk.ac.ebi.biostd.exporter.jobs.partial.PartialSubmissionExporter;


/**
 * Contains job scheduler, note that expression follows cron expression formats where:
 *
 * 1. Seconds 2. Minutes 3. Hours 4. Day-of-Month 5. Month 6. Day-of-Week 7. Year (optional)
 */
@Slf4j
@Component
@AllArgsConstructor
public class JobsScheduler {

    private final FullSubmissionExporter fullExporter;
    private final PartialSubmissionExporter partialExporter;

    @Scheduled(cron = "${jobs.full.cron}", zone = "Europe/London")
    public void generateFullExportFile() {
        fullExporter.execute();
    }

    @Scheduled(cron = "${jobs.partial.cron}", zone = "Europe/London")
    public void generatePartialExportFile() {
        partialExporter.execute();
    }
}
