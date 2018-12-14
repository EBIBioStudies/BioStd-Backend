package uk.ac.ebi.biostd.exporter.jobs.stats;

import static java.lang.String.format;
import static uk.ac.ebi.biostd.exporter.jobs.stats.StatsProperties.FORK_BATCH_SIZE;
import static uk.ac.ebi.biostd.exporter.jobs.stats.StatsProperties.FORK_JOB;
import static uk.ac.ebi.biostd.exporter.jobs.stats.StatsProperties.WORKER_BATCH_SIZE;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import org.easybatch.core.job.Job;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.common.api.JobsFactory;
import uk.ac.ebi.biostd.exporter.jobs.common.base.BaseJobsFactory;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.jobs.common.easybatch.DbRecordReader;
import uk.ac.ebi.biostd.exporter.jobs.common.job.LogBatchListener;
import uk.ac.ebi.biostd.exporter.jobs.stats.process.StatsProcessor;
import uk.ac.ebi.biostd.exporter.persistence.dao.SubmissionDao;

@Component
@AllArgsConstructor
public class StatsJobsFactory extends BaseJobsFactory implements JobsFactory {

    private final SubmissionDao submissionDao;
    private final DataSource dataSource;
    private final StatsProcessor statsProcessor;

    @Override
    public Job newForkJob(List<BlockingQueue<Record>> workQueues) {
        return newForkJob(
                FORK_BATCH_SIZE,
                FORK_JOB,
                new DbRecordReader<>(submissionDao::getStatsSubmissions, dataSource),
                workQueues);
    }

    @Override
    public QueueJob newWorkerJob(int index, BlockingQueue<Record> workQueue,
            List<BlockingQueue<Record>> joinQueues) {

        String name = format("worker-%d", index);

        return workerJob(
                WORKER_BATCH_SIZE,
                name,
                workQueue,
                joinQueues,
                statsProcessor,
                new LogBatchListener(name));
    }
}
