package uk.ac.ebi.biostd.exporter.jobs.full.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Header;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.model.Submission;

@Component
public class SubmissionJsonProcessor implements RecordProcessor<Record<Submission>, Record<String>> {

    private final ObjectMapper objectMapper;

    public SubmissionJsonProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Record processRecord(Record<Submission> record) throws Exception {
        if (record.getPayload() instanceof Submission) {
            return new JsonRecord(record.getHeader(), objectMapper.writeValueAsString(record.getPayload()));
        }

        return record;
    }

    @AllArgsConstructor
    private class JsonRecord implements Record<String> {

        private final Header header;
        private final String json;

        @Override
        public Header getHeader() {
            return header;
        }

        @Override
        public String getPayload() {
            return json;
        }
    }
}
