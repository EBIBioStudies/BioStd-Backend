package uk.ac.ebi.biostd.exporter.jobs.common.api;

import java.util.List;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;

public interface ExportJob {

    void writeJobStats(ExecutionStats stats);

    List<QueueJob> getJoinJob(int workers);

    int getWorkers();
}
