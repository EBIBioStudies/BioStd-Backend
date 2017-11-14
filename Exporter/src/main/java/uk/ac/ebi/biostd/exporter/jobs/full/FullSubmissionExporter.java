package uk.ac.ebi.biostd.exporter.jobs.full;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.job.Job;
import org.easybatch.core.job.JobExecutor;
import org.easybatch.core.job.JobReport;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biostd.exporter.jobs.full.job.SubmissionJobsFactory;
import uk.ac.ebi.biostd.exporter.jobs.full.model.TaskReports;
import uk.ac.ebi.biostd.exporter.jobs.full.model.WorkerJob;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;
import uk.ac.ebi.biostd.exporter.utils.JsonUtil;

/**
 * Main execution class, execute pipeline , write stats into submissions output file.
 */
@Slf4j
@Component
@AllArgsConstructor
public class FullSubmissionExporter {

    private final FullExportJobProperties configProperties;
    private final SubmissionJobsFactory jobsFactory;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public String execute() {
        log.info("executing full export file job at {}", Instant.now());
        int workers = configProperties.getWorkers();
        TaskReports reports = execute(workers);
        ExecutionStats stats = ExecutionStats.builder()
                .startTimeTS(reports.getStartTime())
                .endTimeTS(reports.getEndTime())
                .submissions(reports.getForkJobReport().getMetrics().getWriteCount())
                .threads(workers)
                .errors(reports.getAll().mapToLong(report -> report.getMetrics().getErrorCount()).sum())
                .build();
        writeJobStats(configProperties.getFilePath() + configProperties.getFileName(), stats);
        log.info("finish processing submissions {}", stats);
        return "ok";
    }


    @SneakyThrows
    private void writeJobStats(String fileName, ExecutionStats stats) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.append(",");
            writer.append(JsonUtil.unWrapJsonObject(objectMapper.writeValueAsString(stats)));
            writer.append("\n}");
        }
    }

    @SneakyThrows
    private TaskReports execute(int workers) {
        BlockingQueue<Record> joinQueue = new LinkedBlockingQueue<>();

        List<WorkerJob> workerJobs = createWorkersJobs(workers, joinQueue);
        Job forkJob = jobsFactory.buildForkJob("fork-job", getQueues(workerJobs));
        Job joinJob = jobsFactory.buildJoinJob("join-job", joinQueue, workers);

        JobExecutor jobExecutor = new JobExecutor(workers + 2);

        long startTime = System.currentTimeMillis();
        Future<JobReport> forkReport = jobExecutor.submit(forkJob);
        Future<JobReport> joinReport = jobExecutor.submit(joinJob);
        List<Future<JobReport>> workersReports = jobExecutor.submitAll(getJobs(workerJobs));
        jobExecutor.shutdown();
        long endTime = System.currentTimeMillis();

        return new TaskReports(forkReport.get(), joinReport.get(), getJobReports(workersReports), startTime, endTime);
    }

    @SneakyThrows
    private List<JobReport> getJobReports(List<Future<JobReport>> workersReports) {
        List<JobReport> reports = new ArrayList<>(workersReports.size());
        for (Future<JobReport> report : workersReports) {
            reports.add(report.get());
        }

        return reports;
    }

    private List<WorkerJob> createWorkersJobs(int workers, BlockingQueue<Record> joinQueue) {
        return IntStream.range(0, workers)
                .mapToObj(index -> getWorkerJob(index, joinQueue))
                .collect(toList());
    }

    private WorkerJob getWorkerJob(int index, BlockingQueue<Record> joinQueue) {
        BlockingQueue<Record> queue = new LinkedBlockingQueue<>();
        return new WorkerJob(queue, jobsFactory.buildWorkerJob(index, queue, joinQueue));
    }

    private List<Job> getJobs(List<WorkerJob> workers) {
        return workers.stream().map(WorkerJob::getJob).collect(toList());
    }

    private List<BlockingQueue<Record>> getQueues(List<WorkerJob> workers) {
        return workers.stream().map(WorkerJob::getJoinQueue).collect(toList());
    }
}

