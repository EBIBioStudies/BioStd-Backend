package uk.ac.ebi.biostd.exporter.jobs.full.job;

import static java.lang.String.format;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import org.easybatch.core.job.Job;
import org.easybatch.core.listener.BatchListener;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.common.base.BaseJobsFactory;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.jobs.common.easybatch.DbRecordReader;
import uk.ac.ebi.biostd.exporter.persistence.dao.SubmissionDao;

@Component
@AllArgsConstructor
public class SubmissionJobsFactory extends BaseJobsFactory {

    private static final int WORKER_BATCH_SIZE = 50;
    private static final int FORK_BATCH_SIZE = 100;

    private final SubmissionDao submissionDao;
    private final DataSource dataSource;
    private final SubmissionProcessor submissionProcessor;

    public Job newForkJob(String jobName, List<BlockingQueue<Record>> workQueues) {
        return newForkJob(
                FORK_BATCH_SIZE,
                jobName,
                new DbRecordReader<>(submissionDao::getSubmissions, dataSource),
                workQueues);
    }

    public QueueJob newWorkerJob(int id, BatchListener batchListener, BlockingQueue<Record> workQueue,
            List<BlockingQueue<Record>> joinQueues) {

        return workerJob(
                WORKER_BATCH_SIZE,
                format("worker-%d", id),
                workQueue,
                joinQueues,
                submissionProcessor,
                batchListener);
    }
}
