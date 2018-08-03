package uk.ac.ebi.biostd.exporter.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.common.api.ExportPipeline;
import uk.ac.ebi.biostd.exporter.jobs.partial.PartialSubmissionExporter;

/**
 * Creates bean for the scheduled tasks of the system. The cron expression defined in the config file must
 * follow the format:
 *
 * 1. Seconds 2. Minutes 3. Hours 4. Day-of-Month 5. Month 6. Day-of-Week
 */
@Slf4j
@Component
public class ScheduledTasksConfiguration {
    private final ExportPipeline fullExporter;
    private final ExportPipeline pmcExporter;
    private final ExportPipeline statsExporter;
    private final PartialSubmissionExporter partialExporter;

    @Value("${jobs.dummy.cron}")
    private String dummyCron;

    @Value("${jobs.full.cron:''}")
    private String fullCron;

    @Value("${jobs.partial.cron:''}")
    private String partialCron;

    @Value("${jobs.pmc.export.cron:''}")
    private String pmcCron;

    @Value("${jobs.stats.cron:''}")
    private String statsCron;

    public ScheduledTasksConfiguration(
            @Qualifier("full") ExportPipeline fullExporter,
            @Qualifier("pmc") ExportPipeline pmcExporter,
            @Qualifier("stats") ExportPipeline statsExporter,
            PartialSubmissionExporter partialExporter) {
        this.fullExporter = fullExporter;
        this.partialExporter = partialExporter;
        this.pmcExporter = pmcExporter;
        this.statsExporter = statsExporter;
    }

    @Bean
    @ConditionalOnProperty(prefix = "jobs.dummy", name="enabled", havingValue="true")
    public CronTask dummyScheduler() {
        return new CronTask(() -> log.info("Hi there, I'm the dummy task"), dummyCron);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jobs.full", name="enabled", havingValue="true")
    public CronTask fullScheduler() {
        return new CronTask(() -> fullExporter.execute(), fullCron);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jobs.partial", name="enabled", havingValue="true")
    public CronTask partialScheduler() {
        return new CronTask(() -> partialExporter.execute(), partialCron);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jobs.pmc", name="enabled", havingValue="true")
    public CronTask pmcScheduler() {
        return new CronTask(() -> pmcExporter.execute(), pmcCron);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jobs.stats", name="enabled", havingValue="true")
    public CronTask statsScheduler() {
        return new CronTask(() -> statsExporter.execute(), statsCron);
    }
}
