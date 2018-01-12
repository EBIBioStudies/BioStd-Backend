package uk.ac.ebi.biostd.exporter.jobs.common.api;

import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;

public interface ExportJob {

    void writeJobStats(ExecutionStats stats);

    QueueJob getJoinJob(int workers);
}
