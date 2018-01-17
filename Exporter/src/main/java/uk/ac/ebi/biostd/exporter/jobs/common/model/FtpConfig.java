package uk.ac.ebi.biostd.exporter.jobs.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FtpConfig {

    private String user;
    private String pass;
    private String server;
    private int ftpPort;
    private String outputFolder;
    private String fileNameFormat;

}
