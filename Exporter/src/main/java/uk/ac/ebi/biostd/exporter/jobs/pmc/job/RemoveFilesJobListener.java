package uk.ac.ebi.biostd.exporter.jobs.pmc.job;

import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.easybatch.core.job.JobParameters;
import org.easybatch.core.job.JobReport;
import org.easybatch.core.listener.JobListener;
import uk.ac.ebi.biostd.exporter.jobs.common.model.FtpConfig;

@AllArgsConstructor
@Slf4j
public class RemoveFilesJobListener implements JobListener {

    private final FtpConfig ftpConfig;

    @Override
    public void beforeJobStart(JobParameters jobParameters) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpConfig.getServer(), ftpConfig.getFtpPort());
            ftpClient.login(ftpConfig.getUser(), ftpConfig.getPass());

            FTPFile[] files = ftpClient.listFiles(ftpConfig.getOutputFolder());
            for (FTPFile ftpFile : files) {
                ftpClient.deleteFile(ftpConfig.getOutputFolder() + "/" + ftpFile.getName());
            }
        } catch (IOException e) {
            log.error("error performing ftp operation", e);
        }
    }

    @Override
    public void afterJobEnd(JobReport jobReport) {

    }
}
