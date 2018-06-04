package uk.ac.ebi.biostd.exporter.jobs.stats;

import static org.easybatch.core.job.JobBuilder.aNewJob;
import static uk.ac.ebi.biostd.exporter.jobs.stats.StatsProperties.JOIN_JOB;
import static uk.ac.ebi.biostd.exporter.jobs.stats.StatsProperties.MAX_RECORDS;
import static uk.ac.ebi.biostd.exporter.jobs.stats.StatsProperties.QUEUE_SIZE;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.filter.PoisonRecordFilter;
import org.easybatch.core.job.Job;
import org.easybatch.core.reader.BlockingQueueRecordReader;
import org.easybatch.core.record.Record;
import org.easybatch.core.writer.FileRecordWriter;
import org.easybatch.extensions.apache.common.csv.ApacheCommonCsvRecordMarshaller;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.common.api.ExportJob;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.jobs.stats.model.SubStats;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;

@Slf4j
@Component
@AllArgsConstructor
public class StatsExport implements ExportJob {

    private static final String[] FIELDS = {"accNo", "subFileSize", "files", "filesSize"};

    private final StatsProperties properties;

    @Getter
    private final BlockingQueue<Record> processQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);

    @Override
    public void writeJobStats(ExecutionStats stats) {
        log.info("completed stats export pipeline, {}", stats);
    }

    @Override
    @SneakyThrows
    public List<QueueJob> getJoinJob(int workers) {
        Job job = aNewJob()
                .named(JOIN_JOB)
                .batchSize(MAX_RECORDS)
                .reader(new BlockingQueueRecordReader(processQueue, workers))
                .filter(new PoisonRecordFilter())
                .marshaller(new ApacheCommonCsvRecordMarshaller<>(SubStats.class, FIELDS))
                .writer(new FileRecordWriter(properties.getOutFilePath(), "\n"))
                .build();

        return Collections.singletonList(new QueueJob(processQueue, job));
    }

    @Override
    public int getWorkers() {
        return properties.getWorkers();
    }
}
