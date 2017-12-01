package uk.ac.ebi.biostd.exporter.jobs.full;

import static java.util.stream.Collectors.toList;

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
import uk.ac.ebi.biostd.exporter.jobs.full.job.FullExportJob;
import uk.ac.ebi.biostd.exporter.jobs.full.job.SubmissionJobsFactory;
import uk.ac.ebi.biostd.exporter.jobs.full.model.TaskReports;
import uk.ac.ebi.biostd.exporter.jobs.full.model.WorkerJob;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;

/**
 * Main execution class, execute pipeline , write stats into submissions output file.
 */
@Slf4j
@Component
@AllArgsConstructor
public class FullSubmissionExporter {

    protected final FullExportJobProperties configProperties;
    protected final SubmissionJobsFactory jobsFactory;
    private final List<FullExportJob> exportJobs;

    public final String execute() {
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

        exportJobs.forEach(export -> export.writeJobStats(stats));
        log.info("finish processing submissions {}", stats);
        return "ok";
    }

    @SneakyThrows
    private TaskReports execute(int workers) {
        List<WorkerJob> workerJobs = createWorkersJobs(
                workers,
                exportJobs.stream().map(FullExportJob::getProcessQueue).collect(toList()));

        Job forkJob = jobsFactory.buildForkJob("fork-job", getQueues(workerJobs));
        List<Job> joinJob = exportJobs.stream().map(j -> j.getJoinJob(workers)).collect(toList());

        JobExecutor jobExecutor = new JobExecutor(workers + 1 + exportJobs.size());

        long startTime = System.currentTimeMillis();
        Future<JobReport> forkReport = jobExecutor.submit(forkJob);
        List<Future<JobReport>> workersReports = jobExecutor.submitAll(getJobs(workerJobs));
        jobExecutor.submitAll(joinJob);
        jobExecutor.shutdown();
        long endTime = System.currentTimeMillis();

        return new TaskReports(forkReport.get(), getJobReports(workersReports), startTime, endTime);
    }

    @SneakyThrows
    private List<JobReport> getJobReports(List<Future<JobReport>> workersReports) {
        List<JobReport> reports = new ArrayList<>(workersReports.size());
        for (Future<JobReport> report : workersReports) {
            reports.add(report.get());
        }

        return reports;
    }

    private List<WorkerJob> createWorkersJobs(int workers, List<BlockingQueue<Record>> joinQueues) {
        return IntStream.range(0, workers)
                .mapToObj(index -> getWorkerJob(index, joinQueues))
                .collect(toList());
    }

    private WorkerJob getWorkerJob(int index, List<BlockingQueue<Record>> joinQueues) {
        BlockingQueue<Record> queue = new LinkedBlockingQueue<>();
        return new WorkerJob(queue, jobsFactory.getWorkerJob(index, queue, joinQueues));
    }

    private List<Job> getJobs(List<WorkerJob> workers) {
        return workers.stream().map(WorkerJob::getJob).collect(toList());
    }

    private List<BlockingQueue<Record>> getQueues(List<WorkerJob> workers) {
        return workers.stream().map(WorkerJob::getJoinQueue).collect(toList());
    }
}

