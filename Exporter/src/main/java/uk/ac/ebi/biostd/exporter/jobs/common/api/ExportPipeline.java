package uk.ac.ebi.biostd.exporter.jobs.common.api;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.job.Job;
import org.easybatch.core.job.JobExecutor;
import org.easybatch.core.job.JobReport;
import org.easybatch.core.record.Record;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.jobs.common.job.JobsUtil;
import uk.ac.ebi.biostd.exporter.jobs.full.model.TaskReports;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;
import uk.ac.ebi.biostd.exporter.utils.ConcurrentUtil;

@Slf4j
@AllArgsConstructor
public class ExportPipeline {

    private final int workers;
    private final List<ExportJob> exportJobs;
    private final JobsFactory jobsFactory;

    public final String execute() {
        log.info("executing pmc export file job at {}", Instant.now());
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

    private TaskReports execute(int workers) {
        List<QueueJob> joinJobs = JobsUtil.getJoinJobs(workers, exportJobs);
        List<QueueJob> workerJobs = createWorkersJobs(workers, JobsUtil.getExportQueues(joinJobs));
        Job forkJob = jobsFactory.newForkJob(JobsUtil.getWorkersQueues(workerJobs));

        return executeAll(forkJob, JobsUtil.getJobs(workerJobs), JobsUtil.getJobs(joinJobs));
    }

    private TaskReports executeAll(Job forkJob, List<Job> workersJobs, List<Job> joinJobs) {
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
                .workJobReports(ConcurrentUtil.resolveFutures(jobsFutures.subList(joinJobs.size() + 1, jobs.size())))
                .startTime(startTime)
                .endTime(endTime).build();
    }

    private List<QueueJob> createWorkersJobs(int workers, List<BlockingQueue<Record>> joinQueues) {
        return IntStream.range(0, workers)
                .mapToObj(index -> jobsFactory.newWorkerJob(index, new LinkedBlockingQueue<>(), joinQueues))
                .collect(toList());
    }
}
