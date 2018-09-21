package uk.ac.ebi.biostd.exporter.jobs.full.json;

import static java.util.Collections.singletonMap;
import static uk.ac.ebi.biostd.exporter.jobs.full.job.SubmissionExporter.JSON_EXTENSION;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.filter.PoisonRecordFilter;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.jobs.full.configuration.FullExportJobProperties;
import uk.ac.ebi.biostd.exporter.jobs.full.job.FullExportJob;
import uk.ac.ebi.biostd.exporter.jobs.full.job.SubmissionExporter;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;
import uk.ac.ebi.biostd.exporter.persistence.dao.MetricsDao;
import uk.ac.ebi.biostd.exporter.utils.JsonUtil;

/**
 * Main execution class, execute pipeline , write stats into submissions output file.
 */
@Slf4j
@Component
@AllArgsConstructor
public final class JsonSubmissionExporter implements FullExportJob {
    private static final String JOB_NAME = "join-job-json";

    private final JsonSubmissionProcessor jsonSubmissionProcessor;
    private final MetricsDao metricsDao;
    private final ObjectMapper objectMapper;
    private final FullExportJobProperties jobProperties;
    private final SubmissionExporter submissionExporter;

    @Getter
    private final BlockingQueue<Record> processQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);

    @Override
    @SneakyThrows
    public QueueJob getJoinJob(int workers) {
        return submissionExporter.getJoinJob(
                workers,
                JOB_NAME,
                processQueue,
                jsonSubmissionProcessor,
                new PoisonRecordFilter(),
                jobProperties,
                jobProperties.getAllSubmissions(),
                JSON_EXTENSION);
    }

    @Override
    @SneakyThrows
    public void writeJobStats(ExecutionStats stats) {
        String filePath = submissionExporter.buildFileName(jobProperties.getAllSubmissions(), JSON_EXTENSION);
        stats = stats.toBuilder().
                metrics(singletonMap("@totalFileSize", metricsDao.getTotalFileSize()))
                .build();

        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.append(",");
            writer.append(JsonUtil.unWrapJsonObject(objectMapper.writeValueAsString(stats)));
            writer.append("\n}");
        }
    }
}
