package uk.ac.ebi.biostd.exporter.jobs.dummy;


import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.job.Job;
import org.easybatch.core.job.JobBuilder;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.reader.IterableRecordReader;
import org.easybatch.core.record.Batch;
import org.easybatch.core.record.StringRecord;
import org.easybatch.core.writer.RecordWriter;

@Slf4j
public class DummyJob {

    private final Job job = createJob();

    public void execute() {
        job.call();
    }

    private Job createJob() {
        return new JobBuilder()
                .reader(new IterableRecordReader(Arrays.asList("a", "b", "c")))
                .processor(toUpperCase())
                .writer(new ConsoleRecordWriter())
                .batchSize(1)
                .build();
    }

    private RecordProcessor<StringRecord, StringRecord> toUpperCase() {
        return record -> new StringRecord(record.getHeader(), record.getPayload().toUpperCase());
    }

    private class ConsoleRecordWriter implements RecordWriter {

        @Override
        public void open() throws Exception {

        }

        @Override
        public void writeRecords(Batch batch) throws Exception {
            batch.iterator().forEachRemaining(record -> log.info("received record {}", record.getPayload()));
        }

        @Override
        public void close() throws Exception {

        }
    }
}
