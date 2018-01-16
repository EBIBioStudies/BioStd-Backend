package uk.ac.ebi.biostd.exporter.jobs.full.job;

import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;

public interface FullExportJob {

    int BATCH_SIZE = 250;
    int QUEUE_SIZE = 5000;

    QueueJob getJoinJob(int workers);

    void writeJobStats(ExecutionStats stats);
}
