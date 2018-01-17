package uk.ac.ebi.biostd.exporter.jobs.full;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.common.api.ExportJob;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.jobs.full.job.FullExportJob;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;

@Component
@AllArgsConstructor
public class FullExport implements ExportJob {

    private final List<FullExportJob> exportJobs;

    @Override
    public void writeJobStats(ExecutionStats stats) {
        exportJobs.forEach(export -> export.writeJobStats(stats));
    }

    @Override
    public List<QueueJob> getJoinJob(int workers) {
        return exportJobs.stream().map(job -> job.getJoinJob(workers)).collect(Collectors.toList());
    }
}
