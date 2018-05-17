package uk.ac.ebi.biostd.exporter.jobs.stats;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jobs.stats")
public class StatsProperties {

    static final int QUEUE_SIZE = 3000;
    static final int MAX_RECORDS = 200;
    static final int WORKER_BATCH_SIZE = 50;
    static final int FORK_BATCH_SIZE = 200;
    static final String FORK_JOB = "stats-fork-job";
    static final String JOIN_JOB = "stats-join-job";

    private String basePath;
    private String outFilePath;
    private int workers;
}
