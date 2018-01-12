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
import org.easybatch.core.listener.BatchListener;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.jobs.common.job.LogBatchListener;
import uk.ac.ebi.biostd.exporter.jobs.full.job.FullExportJob;
import uk.ac.ebi.biostd.exporter.jobs.full.job.SubmissionJobsFactory;
import uk.ac.ebi.biostd.exporter.jobs.full.model.TaskReports;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;
import uk.ac.ebi.biostd.exporter.utils.ConcurrentUtil;

/**
 * Main execution class, execute pipeline , write stats into submissions output file.
 */
@Slf4j
@Component
@AllArgsConstructor
public class FullSubmissionExporter {

    private final FullExportJobProperties configProperties;
    private final SubmissionJobsFactory jobsFactory;
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
                .errors(reports.getAll().stream().mapToLong(report -> report.getMetrics().getErrorCount()).sum())
                .build();

        exportJobs.forEach(export -> export.writeJobStats(stats));
        log.info("finish processing submissions {}", stats);
        return "ok";
    }

    @SneakyThrows
    private TaskReports execute(int workers) {
        List<BlockingQueue<Record>> expQueues = exportJobs.stream().map(FullExportJob::getProcessQueue)
                .collect(toList());
        List<QueueJob> workerJobs = createWorkersJobs(workers, expQueues);

        Job forkJob = jobsFactory.newForkJob("fork-job", getQueues(workerJobs));
        List<Job> joinJobs = exportJobs.stream().map(exportJob -> exportJob.getJoinJob(workers)).collect(toList());

        return executeAll(forkJob, getJobs(workerJobs), joinJobs);
    }

    @SneakyThrows
    public TaskReports executeAll(Job forkJob, List<Job> workersJobs, List<Job> joinJobs) {
        List<Job> jobs = new ArrayList<>();
        jobs.add(forkJob);
        jobs.addAll(joinJobs);
        jobs.addAll(workersJobs);

        JobExecutor jobExecutor = new JobExecutor(jobs.size());
        long startTime = System.currentTimeMillis();
        List<Future<JobReport>> jobsFutures = jobExecutor.submitAll(jobs);
        jobExecutor.shutdown();
        long endTime = System.currentTimeMillis();

        return TaskReports.builder()
                .forkJobReport(ConcurrentUtil.resolveFuture(jobsFutures.get(0)))
                .joinJobReports(ConcurrentUtil.resolveFutures(jobsFutures.subList(1, joinJobs.size() + 1)))
                .workJobReports(ConcurrentUtil.resolveFutures(jobsFutures.subList(joinJobs.size(), jobs.size())))
                .startTime(startTime)
                .endTime(endTime).build();
    }

    @SneakyThrows
    private List<JobReport> getJobReports(List<Future<JobReport>> workersReports) {
        List<JobReport> reports = new ArrayList<>(workersReports.size());
        for (Future<JobReport> report : workersReports) {
            reports.add(report.get());
        }

        return reports;
    }

    private List<QueueJob> createWorkersJobs(int workers, List<BlockingQueue<Record>> joinQueues) {
        LogBatchListener logBatchListener = new LogBatchListener("job-workers");

        return IntStream.range(0, workers)
                .mapToObj(index -> getWorkerJob(index, logBatchListener, joinQueues))
                .collect(toList());
    }

    private QueueJob getWorkerJob(int index, BatchListener batchListener, List<BlockingQueue<Record>> joinQueues) {
        BlockingQueue<Record> queue = new LinkedBlockingQueue<>();
        return jobsFactory.newWorkerJob(index, batchListener, queue, joinQueues);
    }

    private List<Job> getJobs(List<QueueJob> workers) {
        return workers.stream().map(QueueJob::getJob).collect(toList());
    }

    private List<BlockingQueue<Record>> getQueues(List<QueueJob> workers) {
        return workers.stream().map(QueueJob::getWorkQueue).collect(toList());
    }
}

