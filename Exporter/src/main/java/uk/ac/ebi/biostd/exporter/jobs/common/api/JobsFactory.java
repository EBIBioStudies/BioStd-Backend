package uk.ac.ebi.biostd.exporter.jobs.common.api;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.easybatch.core.job.Job;
import org.easybatch.core.record.Record;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;

public interface JobsFactory {

    Job newForkJob(List<BlockingQueue<Record>> workersQueues);

    QueueJob newWorkerJob(int index, BlockingQueue<Record> workQueue, List<BlockingQueue<Record>> joinQueues);
}
