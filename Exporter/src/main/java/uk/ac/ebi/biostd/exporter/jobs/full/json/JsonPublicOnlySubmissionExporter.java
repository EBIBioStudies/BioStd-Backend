package uk.ac.ebi.biostd.exporter.jobs.full.json;

import static uk.ac.ebi.biostd.exporter.jobs.full.job.SubmissionExporter.JSON_EXTENSION;

import java.io.FileWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.jobs.full.configuration.FullExportJobProperties;
import uk.ac.ebi.biostd.exporter.jobs.full.job.FullExportJob;
import uk.ac.ebi.biostd.exporter.jobs.full.job.PublicSubmissionFilter;
import uk.ac.ebi.biostd.exporter.jobs.full.job.SubmissionExporter;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;

@Component
@AllArgsConstructor
public final class JsonPublicOnlySubmissionExporter implements FullExportJob {
    private static final String JOB_NAME = "join-job-public-only";

    private final JsonSubmissionProcessor jsonSubmissionProcessor;
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
                jsonSubmissionProcessor,
                new PublicSubmissionFilter(),
                jobProperties.getPublicOnlySubmissions(),
                JSON_EXTENSION);
    }

    @Override
    @SneakyThrows
    public void writeJobStats(ExecutionStats stats) {
        String filePath = submissionExporter.buildFileName(jobProperties.getPublicOnlySubmissions(), JSON_EXTENSION);
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.append("\n}");
        }
    }
}
