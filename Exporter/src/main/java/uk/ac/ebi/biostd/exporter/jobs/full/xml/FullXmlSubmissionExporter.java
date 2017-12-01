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
import uk.ac.ebi.biostd.exporter.jobs.full.job.FullExportJob;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;

/**
 * Main execution class, execute pipeline , write stats into submissions output file.
 */
@Slf4j
@Component
@AllArgsConstructor
public final class FullXmlSubmissionExporter implements FullExportJob {

    private static final String EXTENSION = ".xml";

    private final SubmissionXmlProcessor submissionXmlProcessor;
    private final FullExportJobProperties jobProperties;

    @Getter
    private final BlockingQueue<Record> processQueue = new LinkedBlockingQueue<>();

    @Override
    public Job getJoinJob(int workers) {
        return aNewJob()
                .named("join-job")
                .reader(new BlockingQueueRecordReader(processQueue, workers))
                .filter(new PoisonRecordFilter())
                .processor(submissionXmlProcessor)
                .writer(new BufferedXmlFileWriter(getFileName()))
                .build();
    }

    @Override
    public void writeJobStats(ExecutionStats stats) {

    }

    private String getFileName() {
        return jobProperties.getFilePath() + jobProperties.getFileName() + EXTENSION;
    }

}

