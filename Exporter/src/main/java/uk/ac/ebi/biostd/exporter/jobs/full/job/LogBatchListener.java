package uk.ac.ebi.biostd.exporter.jobs.full.job;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.listener.BatchListener;
import org.easybatch.core.record.Batch;

@Slf4j
@AllArgsConstructor
public class LogBatchListener implements BatchListener {

    private final AtomicInteger batchCount = new AtomicInteger(1);
    private final String jobName;

    @Override
    public void beforeBatchReading() {
    }

    @Override
    public void afterBatchProcessing(Batch batch) {
    }

    @Override
    public void afterBatchWriting(Batch batch) {
        log.info("job: '{}', processed batch {} with size {}", jobName, batchCount.getAndIncrement(), batch.size());
    }

    @Override
    public void onBatchWritingException(Batch batch, Throwable throwable) {
    }
}
