package uk.ac.ebi.biostd.exporter.schedulers;

import java.util.TimeZone;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.common.api.ExportPipeline;
import uk.ac.ebi.biostd.exporter.jobs.partial.PartialSubmissionExporter;


/**
 * Contains job scheduler, note that expression follows cron expression formats where:
 *
 * 1. Seconds 2. Minutes 3. Hours 4. Day-of-Month 5. Month 6. Day-of-Week 7. Year (optional)
 */
@Slf4j
@Component
public class JobsScheduler {

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Europe/London");

    private final ExportPipeline fullExporter;
    private final ExportPipeline pmcExporter;
    private final ExportPipeline statsExporter;
    private final PartialSubmissionExporter partialExporter;
    private final TaskScheduler taskScheduler;

    @Value("${jobs.full.enabled:false}")
    private boolean enabledFullExport;

    @Value("${jobs.full.cron:''}")
    private String fullCron;

    @Value("${jobs.partial.enabled:false}")
    private boolean enabledPartial;

    @Value("${jobs.partial.cron:''}")
    private String partialCron;

    @Value("${jobs.pmc.export.enabled:false}")
    private boolean enablePmc;

    @Value("${jobs.dummy.enabled}")
    private boolean enableDummy;

    @Value("${jobs.dummy.cron}")
    private String dummyCron;

    @Value("${jobs.pmc.export.cron:''}")
    private String pmcCron;

    @Value("${jobs.stats.enabled:false}")
    private boolean enableStats;

    @Value("${jobs.stats.cron:''}")
    private String statsCron;

    public JobsScheduler(
            @Qualifier("full") ExportPipeline fullExporter,
            @Qualifier("pmc") ExportPipeline pmcExporter,
            @Qualifier("stats") ExportPipeline statsExporter,
            PartialSubmissionExporter partialExporter,
            TaskScheduler taskScheduler) {
        this.fullExporter = fullExporter;
        this.partialExporter = partialExporter;
        this.pmcExporter = pmcExporter;
        this.statsExporter = statsExporter;
        this.taskScheduler = taskScheduler;
    }

    @PostConstruct
    public void setupScheduling() {
        if (enabledFullExport) {
            taskScheduler.schedule(fullExporter::execute, new CronTrigger(fullCron, TIME_ZONE));
        }

        if (enabledPartial) {
            taskScheduler.schedule(partialExporter::execute, new CronTrigger(partialCron, TIME_ZONE));
        }

        if (enablePmc) {
            taskScheduler.schedule(pmcExporter::execute, new CronTrigger(pmcCron, TIME_ZONE));
        }

        if (enableStats) {
            taskScheduler.schedule(statsExporter::execute, new CronTrigger(statsCron, TIME_ZONE));
        }
    }
}
