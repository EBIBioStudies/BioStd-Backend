package uk.ac.ebi.biostd.exporter.jobs.full.model;

import java.util.concurrent.BlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.easybatch.core.job.Job;
import org.easybatch.core.record.Record;

@Getter
@AllArgsConstructor
public class WorkerJob {

    private final BlockingQueue<Record> workQueue;
    private final Job job;
}
