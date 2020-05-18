package uk.ac.ebi.biostd.exporter.schedulers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.common.api.ExportPipeline;
import uk.ac.ebi.biostd.exporter.jobs.partial.PartialExportCleaner;
import uk.ac.ebi.biostd.exporter.jobs.partial.PartialSubmissionExporter;
import uk.ac.ebi.biostd.exporter.jobs.releaser.ReleaserJob;

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
    private final PartialExportCleaner partialExportCleaner;
    private final ReleaserJob releaserJob;

    @Value("${jobs.dummy.cron}")
    private String dummyCron;

    @Value("${jobs.full.cron:''}")
    private String fullCron;

    @Value("${jobs.partial.cron:''}")
    private String partialCron;

    @Value("${jobs.partial.cleanerCron:''}")
    private String partialCleanerCron;

    @Value("${jobs.pmc.export.cron:''}")
    private String pmcCron;

    @Value("${jobs.stats.cron:''}")
    private String statsCron;

    @Value("${jobs.releaser.cron:''}")
    private String releaserCron;

    public ScheduledTasksConfiguration(
            @Qualifier("full") ExportPipeline fullExporter,
            @Qualifier("pmc") ExportPipeline pmcExporter,
            @Qualifier("stats") ExportPipeline statsExporter,
            PartialSubmissionExporter partialExporter,
            PartialExportCleaner partialExportCleaner,
            ReleaserJob releaserJob) {
        this.fullExporter = fullExporter;
        this.partialExporter = partialExporter;
        this.partialExportCleaner = partialExportCleaner;
        this.pmcExporter = pmcExporter;
        this.statsExporter = statsExporter;
        this.releaserJob = releaserJob;
    }

    @Bean
    @ConditionalOnProperty(prefix = "jobs.dummy", name = "enabled", havingValue = "true")
    public CronTask dummyScheduler() {
        return new CronTask(() -> log.info("Hi there, I'm the dummy task"), dummyCron);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jobs.full", name = "enabled", havingValue = "true")
    public CronTask fullScheduler() {
        return new CronTask(new FullExportJob(fullExporter::execute), fullCron);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jobs.partial", name = "enabled", havingValue = "true")
    public CronTask partialScheduler() {
        return new CronTask(new PartialExportJob(partialExporter::execute), partialCron);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jobs.partial", name = "enabled", havingValue = "true")
    public CronTask partialCleanerScheduler() {
        return new CronTask(new PartialExportCleanerJob(partialExportCleaner::execute), partialCleanerCron);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jobs.pmc.export", name = "enabled", havingValue = "true")
    public CronTask pmcScheduler() {
        return new CronTask(new PmcLinksExportJob(pmcExporter::execute), pmcCron);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jobs.stats", name = "enabled", havingValue = "true")
    public CronTask statsScheduler() {
        return new CronTask(new StartsExportJob(statsExporter::execute), statsCron);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jobs.releaser", name = "enabled", havingValue = "true")
    public CronTask releaser() {
        return new CronTask(releaserJob::execute, releaserCron);
    }

    @AllArgsConstructor
    public static class FullExportJob implements Runnable {

        private final Runnable runnable;

        @Override
        public void run() {
            runnable.run();
        }
    }

    @AllArgsConstructor
    public static class PartialExportJob implements Runnable {

        private final Runnable runnable;

        @Override
        public void run() {
            runnable.run();
        }
    }

    @AllArgsConstructor
    public static class PartialExportCleanerJob implements Runnable {

        private final Runnable runnable;

        @Override
        public void run() {
            runnable.run();
        }
    }

    @AllArgsConstructor
    public static class PmcLinksExportJob implements Runnable {

        private final Runnable runnable;

        @Override
        public void run() {
            runnable.run();
        }
    }

    @AllArgsConstructor
    public static class StartsExportJob implements Runnable {

        private final Runnable runnable;

        @Override
        public void run() {
            runnable.run();
        }
    }
}
