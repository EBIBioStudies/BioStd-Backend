package uk.ac.ebi.biostd.exporter.jobs.full.json;

import static java.util.Collections.emptyList;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.full.model.StringContentRecord;
import uk.ac.ebi.biostd.exporter.model.Submission;

@Component
public class JsonPublicOnlySubmissionProcessor implements RecordProcessor<Record<Submission>, Record<String>> {
    private final ObjectMapper objectMapper;

    public JsonPublicOnlySubmissionProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Record processRecord(Record<Submission> record) throws Exception {
        if (record.getPayload() instanceof Submission) {
            Submission submission = record.getPayload().toBuilder().accessTags(emptyList()).build();

            return new StringContentRecord(record.getHeader(), objectMapper.writeValueAsString(submission));
        }

        return record;
    }
}
