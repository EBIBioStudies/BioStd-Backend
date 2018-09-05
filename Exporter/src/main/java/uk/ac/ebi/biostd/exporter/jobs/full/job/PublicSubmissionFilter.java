package uk.ac.ebi.biostd.exporter.jobs.full.job;

import org.easybatch.core.filter.RecordFilter;
import org.easybatch.core.record.PoisonRecord;
import org.easybatch.core.record.Record;
import uk.ac.ebi.biostd.exporter.model.Submission;

public class PublicSubmissionFilter implements RecordFilter<Record> {
    public static final String PUBLIC_ACCESS_TAG = "Public";

    @Override
    public Record processRecord(Record record) {
        if (record instanceof PoisonRecord) {
            return null;
        }

        Submission submission = (Submission)record.getPayload();
        return submission.getAccessTags().contains(PUBLIC_ACCESS_TAG) ? record : null;
    }
}
