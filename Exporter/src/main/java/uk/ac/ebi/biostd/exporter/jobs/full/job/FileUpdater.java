package uk.ac.ebi.biostd.exporter.jobs.full.job;

import static java.lang.String.format;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static uk.ac.ebi.biostd.exporter.jobs.full.json.JsonBufferedFileWriter.TEMP_FILE_FORMAT;

import java.nio.file.Paths;
import org.easybatch.core.job.JobParameters;
import org.easybatch.core.job.JobReport;
import org.easybatch.core.listener.JobListener;
import uk.ac.ebi.biostd.exporter.commons.FileUtils;

/**
 * Class in charge of replace export file. It Validates if the number of records are more than the configured
 * threshold so, in case of error, export file is not replaced.
 */
public class FileUpdater implements JobListener {

    private final FileUtils fileUtils;
    private final String fileName;
    private final String tempFileName;
    private final long recordsThreshold;

    FileUpdater(FileUtils fileUtils, String fileName, long recordsThreshold) {
        this.fileUtils = fileUtils;
        this.fileName = fileName;
        this.tempFileName = format(TEMP_FILE_FORMAT, fileName);
        this.recordsThreshold = recordsThreshold;
    }

    @Override
    public void beforeJobStart(JobParameters jobParameters) {
    }

    @Override
    public void afterJobEnd(JobReport jobReport) {
        long records = jobReport.getMetrics().getWriteCount();
        if (records >= recordsThreshold) {
            fileUtils.copy(Paths.get(tempFileName), Paths.get(fileName), REPLACE_EXISTING);
            fileUtils.delete(Paths.get(tempFileName));
        }
    }
}
