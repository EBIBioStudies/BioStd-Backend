package uk.ac.ebi.biostd.exporter.jobs.full.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.easybatch.core.job.JobReport;

/**
 * Contains all the jobs pipeline execution stats.
 */
@Getter
@Builder
public class TaskReports {

    private final long startTime;
    private final long endTime;

    private final JobReport forkJobReport;
    private final List<JobReport> joinJobReports;
    private final List<JobReport> workJobReports;

    public List<JobReport> getAll() {
        List<JobReport> reports = new ArrayList<>();
        reports.add(forkJobReport);
        reports.addAll(joinJobReports);
        reports.addAll(workJobReports);
        return reports;
    }
}
