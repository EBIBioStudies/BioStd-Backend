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
            Submission publicSubmission = publicSubmission(record);
            return new StringContentRecord(record.getHeader(), objectMapper.writeValueAsString(publicSubmission));
        }

        return record;
    }

    private Submission publicSubmission(Record<Submission> record) {
        return record
            .getPayload()
            .toBuilder()
            .accessTags(emptyList())
            .id(null)
            .secretKey(null)
            .relPath(null)
            .rtime(null)
            .ctime(null)
            .mtime(null)
            .views(null)
            .build();
    }
}
