package uk.ac.ebi.biostd.exporter.jobs.pmc.job;

import static java.lang.String.format;
import static uk.ac.ebi.biostd.exporter.jobs.pmc.PmcExportProperties.LINK_FORMAT;
import static uk.ac.ebi.biostd.exporter.jobs.pmc.PmcExportProperties.PROVIDER_ID;
import static uk.ac.ebi.biostd.exporter.jobs.pmc.PmcExportProperties.SOURCE;

import java.util.Date;
import lombok.AllArgsConstructor;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.GenericRecord;
import org.easybatch.core.record.Header;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.pmc.model.Link;
import uk.ac.ebi.biostd.exporter.jobs.pmc.model.PmcRecord;
import uk.ac.ebi.biostd.exporter.jobs.pmc.model.Resource;
import uk.ac.ebi.biostd.exporter.model.Submission;
import uk.ac.ebi.biostd.exporter.persistence.dao.SubmissionDao;

@AllArgsConstructor
@Component
public class PmcRecordProcessor implements RecordProcessor<Record, Record> {

    private final String RECORD_SOURCE = PmcRecordProcessor.class.getName();
    private final SubmissionDao submissionDao;

    @Override
    public Record processRecord(Record record) {
        if (record.getPayload() instanceof Submission) {
            Submission submission = (Submission) record.getPayload();

            Link link = new Link();
            link.setProviderId(PROVIDER_ID);

            Resource resource = new Resource();
            resource.setTitle(submission.getTitle());
            resource.setUrl(format(LINK_FORMAT, submission.getAccno()));
            link.setResource(resource);

            PmcRecord pmcRecord = new PmcRecord();
            pmcRecord.setSource(SOURCE);
            pmcRecord.setId(submissionDao.getPublicationId(submission.getId()));
            link.setRecord(pmcRecord);

            return new GenericRecord<>(new Header(submission.getId(), RECORD_SOURCE, new Date()), link);
        }

        return record;
    }
}
