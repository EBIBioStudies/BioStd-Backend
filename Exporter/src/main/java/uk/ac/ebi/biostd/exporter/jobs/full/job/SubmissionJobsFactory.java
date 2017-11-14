package uk.ac.ebi.biostd.exporter.jobs.full.job;

import static org.easybatch.core.job.JobBuilder.aNewJob;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.easybatch.core.filter.PoisonRecordFilter;
import org.easybatch.core.job.Job;
import org.easybatch.core.listener.PoisonRecordBroadcaster;
import org.easybatch.core.reader.BlockingQueueRecordReader;
import org.easybatch.core.record.Record;
import org.easybatch.core.writer.BlockingQueueRecordWriter;
import org.easybatch.core.writer.RoundRobinBlockingQueueRecordWriter;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.full.FullExportJobProperties;

@Component
@AllArgsConstructor
public class SubmissionJobsFactory {

    private final SubmissionJsonProcessor submissionJsonProcessor;
    private final SubmissionProcessor submissionProcessor;
    private final SubmissionRecordReader submissionRecordReader;
    private final FullExportJobProperties properties;

    @SneakyThrows
    public Job buildForkJob(String jobName, List<BlockingQueue<Record>> workQueues) {
        return aNewJob()
                .named(jobName)
                .batchSize(10)
                .reader(submissionRecordReader)
                .writer(new RoundRobinBlockingQueueRecordWriter(workQueues))
                .jobListener(new PoisonRecordBroadcaster(workQueues))
                .build();
    }

    @SneakyThrows
    public Job buildJoinJob(String jobName, BlockingQueue<Record> joinQueue, int poisonRecord) {
        return aNewJob()
                .named(jobName)
                .reader(new BlockingQueueRecordReader(joinQueue, poisonRecord))
                .filter(new PoisonRecordFilter())
                .writer(new BufferedFileWriter(properties.getFilePath() + properties.getFileName()))
                .build();
    }

    @SneakyThrows
    public Job buildWorkerJob(int id, BlockingQueue<Record> workQueue, BlockingQueue<Record> joinQueue) {
        return aNewJob()
                .named(String.format("worker-%d", id))
                .reader(new BlockingQueueRecordReader(workQueue))
                .processor(submissionProcessor)
                .processor(submissionJsonProcessor)
                .writer(new BlockingQueueRecordWriter(joinQueue))
                .build();
    }
}
