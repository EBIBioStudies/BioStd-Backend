package uk.ac.ebi.biostd.exporter.jobs.full.job;

import static org.easybatch.core.job.JobBuilder.aNewJob;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import lombok.AllArgsConstructor;
import org.easybatch.core.job.Job;
import org.easybatch.core.listener.BatchListener;
import org.easybatch.core.listener.PoisonRecordBroadcaster;
import org.easybatch.core.reader.BlockingQueueRecordReader;
import org.easybatch.core.record.Record;
import org.easybatch.core.writer.BlockingQueueRecordWriter;
import org.easybatch.core.writer.RoundRobinBlockingQueueRecordWriter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SubmissionJobsFactory {

    private static final int WORKER_BATCH_SIZE = 50;
    private static final int FORK_BATCH_SIZE = 100;

    private final SubmissionRecordReader submissionRecordReader;
    private final SubmissionProcessor submissionProcessor;

    public Job buildForkJob(String jobName, List<BlockingQueue<Record>> workQueues) {
        return aNewJob()
                .named(jobName)
                .batchSize(FORK_BATCH_SIZE)
                .reader(submissionRecordReader)
                .writer(new RoundRobinBlockingQueueRecordWriter(workQueues))
                .jobListener(new PoisonRecordBroadcaster(workQueues))
                .build();
    }

    public Job getWorkerJob(int id, BatchListener batchListener, BlockingQueue<Record> workQueue,
            List<BlockingQueue<Record>> joinQueues) {
        return aNewJob()
                .batchSize(WORKER_BATCH_SIZE)
                .named(String.format("worker-%d", id))
                .reader(new BlockingQueueRecordReader(workQueue))
                .processor(submissionProcessor)
                .writer(new BlockingQueueRecordWriter(joinQueues))
                .batchListener(batchListener)
                .build();
    }
}
