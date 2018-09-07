package uk.ac.ebi.biostd.exporter.jobs.full.xml;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.filter.PoisonRecordFilter;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.jobs.full.configuration.FullExportJobProperties;
import uk.ac.ebi.biostd.exporter.jobs.full.job.FullExportJob;
import uk.ac.ebi.biostd.exporter.jobs.full.job.SubmissionExporter;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;

/**
 * Main execution class, execute pipeline , write stats into submissions output file.
 */
@Slf4j
@Component
@AllArgsConstructor
public final class XmlSubmissionExporter implements FullExportJob {
    private static final String JOB_NAME = "join-job-xml";

    private final XmlSubmissionProcessor xmlSubmissionProcessor;
    private final FullExportJobProperties jobProperties;
    private final SubmissionExporter submissionExporter;

    @Getter
    private final BlockingQueue<Record> processQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);

    @Override
    public QueueJob getJoinJob(int workers) {
        return submissionExporter.getJoinJob(
                workers,
                JOB_NAME,
                processQueue,
                xmlSubmissionProcessor,
                new PoisonRecordFilter(),
                jobProperties.getAllSubmissions(),
                SubmissionExporter.XML_EXTENSION);
    }

    @Override
    public void writeJobStats(ExecutionStats stats) {
    }
}

