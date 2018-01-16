package uk.ac.ebi.biostd.exporter.jobs.common.job;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import lombok.experimental.UtilityClass;
import org.easybatch.core.job.Job;
import org.easybatch.core.record.Record;
import uk.ac.ebi.biostd.exporter.jobs.common.api.ExportJob;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;

@UtilityClass
public class JobsUtil {

    public List<BlockingQueue<Record>> getExportQueues(List<QueueJob> exportJobs) {
        return exportJobs.stream().map(QueueJob::getWorkQueue).collect(toList());
    }

    public List<BlockingQueue<Record>> getWorkersQueues(List<QueueJob> workers) {
        return workers.stream().map(QueueJob::getWorkQueue).collect(toList());
    }

    public List<Job> getJobs(List<QueueJob> workers) {
        return workers.stream().map(QueueJob::getJob).collect(toList());
    }

    public List<QueueJob> getJoinJobs(int workers, List<ExportJob> exportJobs) {
        return exportJobs.stream().flatMap(job -> job.getJoinJob(workers).stream()).collect(toList());
    }
}
