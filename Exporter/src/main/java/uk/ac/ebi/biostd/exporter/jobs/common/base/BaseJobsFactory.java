package uk.ac.ebi.biostd.exporter.jobs.common.base;

import static org.easybatch.core.job.JobBuilder.aNewJob;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import lombok.AllArgsConstructor;
import org.easybatch.core.job.Job;
import org.easybatch.core.listener.BatchListener;
import org.easybatch.core.listener.PoisonRecordBroadcaster;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.reader.BlockingQueueRecordReader;
import org.easybatch.core.reader.RecordReader;
import org.easybatch.core.record.Record;
import org.easybatch.core.writer.BlockingQueueRecordWriter;
import org.easybatch.core.writer.RoundRobinBlockingQueueRecordWriter;

@AllArgsConstructor
public class BaseJobsFactory {

    protected Job newForkJob(
            int batchSize,
            String jobName,
            RecordReader reader,
            List<BlockingQueue<Record>> workQueues) {
        return aNewJob()
                .named(jobName)
                .batchSize(batchSize)
                .reader(reader)
                .writer(new RoundRobinBlockingQueueRecordWriter(workQueues))
                .jobListener(new PoisonRecordBroadcaster(workQueues))
                .build();
    }

    protected QueueJob workerJob(
            int batchSize,
            String jobName,
            BlockingQueue<Record> workQueue,
            List<BlockingQueue<Record>> joinQueues,
            RecordProcessor recordProcessor,
            BatchListener batchListener) {
        Job job = aNewJob()
                .batchSize(batchSize)
                .named(jobName)
                .reader(new BlockingQueueRecordReader(workQueue))
                .processor(recordProcessor)
                .writer(new BlockingQueueRecordWriter(joinQueues))
                .batchListener(batchListener)
                .build();

        return new QueueJob(workQueue, job);
    }
}
