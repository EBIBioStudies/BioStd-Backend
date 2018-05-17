package uk.ac.ebi.biostd.exporter.jobs.stats.process;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.GenericRecord;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.model.Submission;

@Slf4j
@Component
@AllArgsConstructor
public class StatsProcessor implements RecordProcessor<Record<Submission>, Record<?>> {

    private final StatsService statsService;

    @Override
    public Record<?> processRecord(Record<Submission> record) {
        if (record.getPayload() instanceof Submission) {
            return new GenericRecord<>(
                    record.getHeader(),
                    statsService.processSubmission(record.getPayload()));
        }

        return record;
    }
}
