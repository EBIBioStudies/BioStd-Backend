package uk.ac.ebi.biostd.exporter.jobs.pmc;

import static org.easybatch.core.job.JobBuilder.aNewJob;
import static uk.ac.ebi.biostd.exporter.jobs.full.job.FullExportJob.QUEUE_SIZE;
import static uk.ac.ebi.biostd.exporter.jobs.pmc.PmcExportProperties.JOIN_JOB;
import static uk.ac.ebi.biostd.exporter.jobs.pmc.PmcExportProperties.MAX_RECORDS;

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
import uk.ac.ebi.biostd.exporter.jobs.common.api.ExportJob;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.jobs.common.easybatch.FtpRecordWriter;
import uk.ac.ebi.biostd.exporter.jobs.common.model.FtpConfig;
import uk.ac.ebi.biostd.exporter.jobs.pmc.job.PmcXmlProcessor;
import uk.ac.ebi.biostd.exporter.jobs.pmc.job.XmlLinksWriter;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;

@Slf4j
@Component
@AllArgsConstructor
public class PmcExport implements ExportJob {

    private final PmcExportProperties properties;

    @Getter
    private final BlockingQueue<Record> processQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);
    private final PmcXmlProcessor pmcXmlProcessor;

    @Override
    public void writeJobStats(ExecutionStats stats) {
        log.info("completed pmc export pipeline, {}", stats);
    }

    @Override
    public QueueJob getJoinJob(int workers) {
        FtpConfig ftpConfig = new FtpConfig(properties.getUser(), properties.getPassword(), properties.getFtpServer());

        Job job = aNewJob()
                .named(JOIN_JOB)
                .batchSize(MAX_RECORDS)
                .reader(new BlockingQueueRecordReader(processQueue, workers))
                .filter(new PoisonRecordFilter())
                .processor(pmcXmlProcessor)
                .writer(new FtpRecordWriter(
                        properties.getOutputFolder(),
                        properties.getFtpPort(),
                        new XmlLinksWriter(),
                        ftpConfig))
                .build();

        return new QueueJob(processQueue, job);
    }

}
