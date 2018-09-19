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
import org.easybatch.core.writer.RecordWriter;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.commons.FileUtils;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.jobs.common.job.LogBatchListener;
import uk.ac.ebi.biostd.exporter.jobs.full.configuration.FullExportFileProperties;
import uk.ac.ebi.biostd.exporter.jobs.full.configuration.FullExportJobProperties;
import uk.ac.ebi.biostd.exporter.jobs.full.json.JsonBufferedFileWriter;
import uk.ac.ebi.biostd.exporter.jobs.full.xml.XmlBufferedFileWriter;

@Component
@AllArgsConstructor
public class SubmissionExporter {
    public static final String XML_EXTENSION = ".xml";
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
                .writer(getRecordWriter(fileName, fileExtension))
                .batchListener(new LogBatchListener(jobName))
                .jobListener(new FileUpdater(fileUtils, fileName, jobProperties.getRecordsThreshold()))
                .build();

        return new QueueJob(processQueue, job);
    }

    public String buildFileName(FullExportFileProperties config, String fileExtension) {
        return config.getFilePath() + config.getFileName() + fileExtension;
    }

    private RecordWriter getRecordWriter(String fileName, String fileExtension) {
        RecordWriter recordWriter;
        switch (fileExtension) {
            case XML_EXTENSION:
                recordWriter = new XmlBufferedFileWriter(fileName);
                break;

            case JSON_EXTENSION:
            default:
                recordWriter = new JsonBufferedFileWriter(fileName);
                break;
        }

        return recordWriter;
    }
}
