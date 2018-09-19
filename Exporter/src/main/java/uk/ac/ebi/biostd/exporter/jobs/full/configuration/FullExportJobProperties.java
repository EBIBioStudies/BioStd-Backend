package uk.ac.ebi.biostd.exporter.jobs.full.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * Contains application configuration properties extracted from configuration file application.yml.
 *
 * {@link #fileName} generated output file name.
 * {@link #filePath} generated output file path.
 * {@link #workers} number of workers thread to process submissions.
 * {@link #queryModified} main submissions query modification i.e limit 100 to process only 100 submissions.
 * {@link #notificationUrl} url no notify when process is completed.
 * {@link #recordsThreshold} minimun number of records to be processed to generate output file.
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "jobs.full")
public class FullExportJobProperties {

    public static final int WORKER_BATCH_SIZE = 50;
    public static final int FORK_BATCH_SIZE = 100;
    public static final long RECORDS_THRESHOLD = 100;

    public static final String FORK_JOB = "full-fork-job";
    public static final String WORK_JOB_NAME_FORMAT = "full-worker-%d";
    public static final String JOIN_JOB = "full-join-job";

    private int workers;
    private String queryModified;
    private String notificationUrl;
    private long recordsThreshold;

    @NestedConfigurationProperty
    private FullExportAllSubmissionsProperties allSubmissions;

    @NestedConfigurationProperty
    private FullExportPublicOnlySubmissionsProperties publicOnlySubmissions;
}
