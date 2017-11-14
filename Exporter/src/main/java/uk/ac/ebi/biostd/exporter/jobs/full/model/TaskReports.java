package uk.ac.ebi.biostd.exporter.jobs.full.model;

import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.easybatch.core.job.JobReport;

/**
 * Contains all the jobs pipeline execution stats.
 */
@Data
@AllArgsConstructor
public class TaskReports {

    private final JobReport forkJobReport;
    private final JobReport joinJobReport;
    private final List<JobReport> workersReports;
    private final long startTime;
    private final long endTime;

    public Stream<JobReport> getAll() {
        return Stream.concat(workersReports.stream(), Stream.of(joinJobReport, forkJobReport));
    }
}
