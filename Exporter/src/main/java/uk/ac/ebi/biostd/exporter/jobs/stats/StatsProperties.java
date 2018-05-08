package uk.ac.ebi.biostd.exporter.jobs.stats;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jobs.stats")
public class StatsProperties {

    public static final int QUEUE_SIZE = 3000;
    public static final int MAX_RECORDS = 200;
    public static final int WORKER_BATCH_SIZE = 50;
    public static final int FORK_BATCH_SIZE = 200;
    public static final String FORK_JOB = "stats-fork-job";
    public static final String JOIN_JOB = "stats-join-job";

    private String basePath;
    private String outFilePath;
    private int workers;
}
