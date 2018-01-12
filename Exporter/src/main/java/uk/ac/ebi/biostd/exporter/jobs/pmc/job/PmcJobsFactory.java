package uk.ac.ebi.biostd.exporter.jobs.pmc.job;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import org.easybatch.core.job.Job;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.common.api.JobsFactory;
import uk.ac.ebi.biostd.exporter.jobs.common.base.BaseJobsFactory;
import uk.ac.ebi.biostd.exporter.jobs.common.base.QueueJob;
import uk.ac.ebi.biostd.exporter.jobs.common.easybatch.DbRecordReader;
import uk.ac.ebi.biostd.exporter.jobs.common.job.LogBatchListener;
import uk.ac.ebi.biostd.exporter.jobs.pmc.PmcExportProperties;
import uk.ac.ebi.biostd.exporter.persistence.dao.SubmissionDao;

@AllArgsConstructor
@Component
public class PmcJobsFactory extends BaseJobsFactory implements JobsFactory {

    private final SubmissionDao submissionDao;
    private final DataSource dataSource;
    private final PmcRecordProcessor pmcRecordProcessor;

    @Override
    public Job newForkJob(List<BlockingQueue<Record>> workersQueues) {
        return super.newForkJob(
                PmcExportProperties.BATCH_SIZE,
                PmcExportProperties.FORK_JOB,
                new DbRecordReader<>(submissionDao::getPmcSubmissions, dataSource),
                workersQueues);
    }

    @Override
    public QueueJob newWorkerJob(int index, LinkedBlockingQueue<Object> objects,
            List<BlockingQueue<Record>> joinQueues) {

        BlockingQueue<Record> blockingQueue = new LinkedBlockingQueue<>();
        String jobName = String.format(PmcExportProperties.WORK_JOB_NAME_FORMAT, index);

        return super.workerJob(
                PmcExportProperties.BATCH_SIZE,
                jobName,
                blockingQueue,
                joinQueues,
                pmcRecordProcessor,
                new LogBatchListener(jobName));
    }
}
