package uk.ac.ebi.biostd.exporter.jobs.full.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.model.Submission;
import uk.ac.ebi.biostd.exporter.service.SubmissionService;

@Component
@AllArgsConstructor
@Slf4j
public class SubmissionProcessor implements RecordProcessor<Record<Submission>, Record<Submission>> {

    private final SubmissionService submissionService;

    @Override
    public Record<Submission> processRecord(Record<Submission> record) throws Exception {
        if (record.getPayload() instanceof Submission) {
            submissionService.processSubmission(record.getPayload());
        }

        return record;
    }
}
