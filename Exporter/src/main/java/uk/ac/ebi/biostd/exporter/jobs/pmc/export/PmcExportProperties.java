package uk.ac.ebi.biostd.exporter.jobs.pmc.export;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jobs.pmc.export")
public class PmcExportProperties {

    public static final int MAX_RECORDS = 4000;
    public static final int BATCH_SIZE = 4;
    public static final String PROVIDER_ID = "1518";
    public static final String LINK_FORMAT = "http://www.ebi.ac.uk/biostudies/studies/%s";
    public static final String SOURCE = "PMC";

    public static final String FORK_JOB = "pmc-fork-job";
    public static final String WORK_JOB_NAME_FORMAT = "pmc-worker-%d";
    public static final String JOIN_JOB = "pmc-join-job";

    private int workers;
    private String user;
    private String password;
    private String ftpServer;
    private int ftpPort;
    private String outputFolder;
    private String fileNameFormat;
}
