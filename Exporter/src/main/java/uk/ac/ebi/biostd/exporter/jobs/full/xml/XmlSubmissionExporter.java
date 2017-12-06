package uk.ac.ebi.biostd.exporter.jobs.full.xml;

import static org.easybatch.core.job.JobBuilder.aNewJob;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.filter.PoisonRecordFilter;
import org.easybatch.core.job.Job;
import org.easybatch.core.reader.BlockingQueueRecordReader;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.full.FullExportJobProperties;
import uk.ac.ebi.biostd.exporter.jobs.full.job.ExportJob;
import uk.ac.ebi.biostd.exporter.jobs.full.job.LogBatchListener;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;

/**
 * Main execution class, execute pipeline , write stats into submissions output file.
 */
@Slf4j
@Component
@AllArgsConstructor
public final class XmlSubmissionExporter implements ExportJob {

    private static final String EXTENSION = ".xml";
    private static final String JOB_NAME = "join-job-xml";

    private final SubmissionXmlProcessor submissionXmlProcessor;
    private final FullExportJobProperties jobProperties;

    @Getter
    private final BlockingQueue<Record> processQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);

    @Override
    public Job getJoinJob(int workers) {
        return aNewJob()
                .named(JOB_NAME)
                .batchSize(BATCH_SIZE)
                .reader(new BlockingQueueRecordReader(processQueue, workers))
                .filter(new PoisonRecordFilter())
                .processor(submissionXmlProcessor)
                .writer(new BufferedXmlFileWriter(getFileName()))
                .batchListener(new LogBatchListener(JOB_NAME))
                .build();
    }

    @Override
    public void writeJobStats(ExecutionStats stats) {
    }

    private String getFileName() {
        return jobProperties.getFilePath() + jobProperties.getFileName() + EXTENSION;
    }

}

