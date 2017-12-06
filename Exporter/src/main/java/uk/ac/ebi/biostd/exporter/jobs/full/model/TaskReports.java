package uk.ac.ebi.biostd.exporter.jobs.full.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.easybatch.core.job.JobReport;

/**
 * Contains all the jobs pipeline execution stats.
 */
@Getter
public class TaskReports {

    private final long startTime;
    private final long endTime;

    private final Map<String, JobReport> jobsMap;

    public TaskReports(List<JobReport> jobReports, long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;

        jobsMap = new HashMap<>(jobReports.size());
        jobReports.forEach(jobReport -> jobsMap.put(jobReport.getJobName(), jobReport));
    }

    public Collection<JobReport> getAll() {
        return jobsMap.values();
    }

    public JobReport getForkReport() {
        return jobsMap.get("fork-job");
    }
}
