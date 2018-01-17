package uk.ac.ebi.biostd.exporter.jobs.full.json;

import static org.easybatch.core.job.JobBuilder.aNewJob;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileWriter;
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
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.jobs.common.job.LogBatchListener;
import uk.ac.ebi.biostd.exporter.jobs.full.FullExportJobProperties;
import uk.ac.ebi.biostd.exporter.jobs.full.job.FullExportJob;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;
import uk.ac.ebi.biostd.exporter.utils.JsonUtil;

/**
 * Main execution class, execute pipeline , write stats into submissions output file.
 */
@Slf4j
@Component
@AllArgsConstructor
public final class JsonSubmissionExporter implements FullExportJob {

    private static final String EXTENSION = ".json";
    private static final String JOB_NAME = "join-job-json";

    private final SubmissionJsonProcessor submissionJsonProcessor;
    private final ObjectMapper objectMapper;
    private final FullExportJobProperties jobProperties;

    @Getter
    private final BlockingQueue<Record> processQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);

    @Override
    @SneakyThrows
    public QueueJob getJoinJob(int workers) {
        Job job = aNewJob()
                .named(JOB_NAME)
                .batchSize(BATCH_SIZE)
                .reader(new BlockingQueueRecordReader(processQueue, workers))
                .filter(new PoisonRecordFilter())
                .processor(submissionJsonProcessor)
                .writer(new JsonBufferedFileWriter(getFileName()))
                .batchListener(new LogBatchListener(JOB_NAME))
                .build();

        return new QueueJob(processQueue, job);
    }

    @Override
    @SneakyThrows
    public void writeJobStats(ExecutionStats stats) {
        try (FileWriter writer = new FileWriter(getFileName(), true)) {
            writer.append(",");
            writer.append(JsonUtil.unWrapJsonObject(objectMapper.writeValueAsString(stats)));
            writer.append("\n}");
        }
    }

    private String getFileName() {
        return jobProperties.getFilePath() + jobProperties.getFileName() + EXTENSION;
    }
}

