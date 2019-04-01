package uk.ac.ebi.biostd.exporter.jobs.full.job;

import static org.easybatch.core.job.JobBuilder.aNewJob;
import static uk.ac.ebi.biostd.exporter.jobs.full.job.FullExportJob.BATCH_SIZE;

import java.util.concurrent.BlockingQueue;
import lombok.AllArgsConstructor;
import org.easybatch.core.filter.RecordFilter;
import org.easybatch.core.job.Job;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.reader.BlockingQueueRecordReader;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.commons.FileUtils;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.jobs.common.job.LogBatchListener;
import uk.ac.ebi.biostd.exporter.jobs.full.configuration.FullExportFileProperties;
import uk.ac.ebi.biostd.exporter.jobs.full.configuration.FullExportJobProperties;
import uk.ac.ebi.biostd.exporter.jobs.full.json.JsonBufferedFileWriter;

@Component
@AllArgsConstructor
public class SubmissionExporter {

    public static final String JSON_EXTENSION = ".json";

    private final FileUtils fileUtils;

    public QueueJob getJoinJob(
            int workers,
            String jobName,
            BlockingQueue<Record> processQueue,
            RecordProcessor recordProcessor,
            RecordFilter recordFilter,
            FullExportJobProperties jobProperties,
            FullExportFileProperties config,
            String fileExtension) {

        String fileName = buildFileName(config, fileExtension);

        Job job = aNewJob()
                .named(jobName)
                .batchSize(BATCH_SIZE)
                .reader(new BlockingQueueRecordReader(processQueue, workers))
                .filter(recordFilter)
                .processor(recordProcessor)
                .writer(new JsonBufferedFileWriter(fileName))
                .batchListener(new LogBatchListener(jobName))
                .jobListener(new FileUpdater(fileUtils, fileName, jobProperties.getRecordsThreshold()))
                .build();

        return new QueueJob(processQueue, job);
    }

    public String buildFileName(FullExportFileProperties config, String fileExtension) {
        return config.getFilePath() + config.getFileName() + fileExtension;
    }
}
