package uk.ac.ebi.biostd.exporter.jobs.full.job;

import java.util.concurrent.BlockingQueue;
import org.easybatch.core.job.Job;
import org.easybatch.core.record.Record;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;

public interface FullExportJob {

    Job getJoinJob(int workers);

    BlockingQueue<Record> getProcessQueue();

    void writeJobStats(ExecutionStats stats);
}
