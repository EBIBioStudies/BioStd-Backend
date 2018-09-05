package uk.ac.ebi.biostd.exporter.jobs.full.json;

import static java.util.Collections.singletonMap;
import static org.easybatch.core.job.JobBuilder.aNewJob;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.easybatch.core.job.Job;
import org.easybatch.core.reader.BlockingQueueRecordReader;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.jobs.common.job.LogBatchListener;
import uk.ac.ebi.biostd.exporter.jobs.full.configuration.FullExportFileProperties;
import uk.ac.ebi.biostd.exporter.jobs.full.configuration.FullExportJobProperties;
import uk.ac.ebi.biostd.exporter.jobs.full.job.FullExportJob;
import uk.ac.ebi.biostd.exporter.jobs.full.job.PublicSubmissionFilter;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;
import uk.ac.ebi.biostd.exporter.persistence.dao.MetricsDao;
import uk.ac.ebi.biostd.exporter.utils.JsonUtil;

@Component
@AllArgsConstructor
public final class JsonPublicOnlySubmissionExporter implements FullExportJob {
    private static final String EXTENSION = ".json";
    private static final String JOB_NAME = "join-job-public-only-json";

    private final SubmissionJsonProcessor submissionJsonProcessor;
    private final MetricsDao metricsDao;
    private final ObjectMapper objectMapper;
    private final FullExportJobProperties jobProperties;

    @Getter
    private final BlockingQueue<Record> processQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);

    @Override
    public QueueJob getJoinJob(int workers) {
        Job job = aNewJob()
                .named(JOB_NAME)
                .batchSize(BATCH_SIZE)
                .reader(new BlockingQueueRecordReader(processQueue, workers))
                .filter(new PublicSubmissionFilter())
                .processor(submissionJsonProcessor)
                .writer(new JsonBufferedFileWriter(getFileName()))
                .batchListener(new LogBatchListener(JOB_NAME))
                .build();

        return new QueueJob(processQueue, job);
    }

    @Override
    @SneakyThrows
    public void writeJobStats(ExecutionStats stats) {
        stats = stats.toBuilder()
                .metrics(singletonMap("@totalFileSize", metricsDao.getPublicOnlyTotalFileSize()))
                .build();

        try (FileWriter writer = new FileWriter(getFileName(), true)) {
            writer.append(",");
            writer.append(JsonUtil.unWrapJsonObject(objectMapper.writeValueAsString(stats)));
            writer.append("\n}");
        }
    }

    private String getFileName() {
        FullExportFileProperties config = jobProperties.getPublicOnlySubmissions();

        return config.getFilePath() + config.getFileName() + EXTENSION;
    }
}
