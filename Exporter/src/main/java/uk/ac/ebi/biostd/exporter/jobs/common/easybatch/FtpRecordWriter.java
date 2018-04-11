package uk.ac.ebi.biostd.exporter.jobs.common.easybatch;

import static java.lang.String.format;
import static uk.ac.ebi.biostd.exporter.error.PostConditions.checkOutput;

import com.google.common.collect.ImmutableList;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import org.apache.commons.net.ftp.FTPClient;
import org.easybatch.core.record.Batch;
import org.easybatch.core.writer.RecordWriter;
import uk.ac.ebi.biostd.exporter.jobs.common.api.DataWriter;
import uk.ac.ebi.biostd.exporter.jobs.common.model.FtpConfig;

@AllArgsConstructor
public class FtpRecordWriter implements RecordWriter {

    private static final FTPClient ftpClient = new FTPClient();
    private final AtomicInteger counter = new AtomicInteger(1);

    private final DataWriter dataWriter;
    private final FtpConfig ftpConfig;

    @Override
    public void open() throws Exception {
        ftpClient.connect(ftpConfig.getServer(), ftpConfig.getFtpPort());
        checkOutput(ftpClient.login(ftpConfig.getUser(), ftpConfig.getPass()), "fail to connect to ftp service");
    }

    @Override
    public void writeRecords(Batch batch) throws Exception {
        InputStream inputStream = dataWriter.getInputStream(ImmutableList.copyOf(batch.iterator()));
        String filePath = format(
                ftpConfig.getOutputFolder() + "/" + ftpConfig.getFileNameFormat(), counter.getAndIncrement());
        checkOutput(ftpClient.storeFile(filePath, inputStream), "fail to upload file to ftp service");
    }

    @Override
    public void close() throws Exception {
        if (ftpClient.isConnected()) {
            ftpClient.logout();
            ftpClient.disconnect();
        }
    }
}
