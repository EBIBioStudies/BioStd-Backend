package uk.ac.ebi.biostd.exporter.jobs.full.job;


import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.easybatch.core.reader.RecordReader;
import org.easybatch.core.record.GenericRecord;
import org.easybatch.core.record.Header;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.model.Submission;
import uk.ac.ebi.biostd.exporter.persistence.dao.SubmissionDao;

@Component
public class SubmissionRecordReader implements RecordReader {

    private final SubmissionDao submissionDao;
    private final String dbSchema;

    private List<Submission> submissionList;
    private int submissions;
    private int currentRecord = 0;

    @SneakyThrows
    public SubmissionRecordReader(SubmissionDao submissionDao, DataSource dataSource) {
        this.submissionDao = submissionDao;
        dbSchema = dataSource.getConnection().getSchema();
    }

    @Override
    public void open() throws Exception {
        submissionList = submissionDao.getSubmissions();
        submissions = submissionList.size();
        currentRecord = 0;
    }

    @Override
    public Record readRecord() throws Exception {
        if (currentRecord < submissions) {
            GenericRecord<Submission> record = new GenericRecord<>(new Header(
                    (long) currentRecord,
                    dbSchema,
                    new Date()),
                    submissionList.get(currentRecord));
            currentRecord++;
            return record;
        }

        return null;
    }

    @Override
    public void close() throws Exception {
        submissionList = null;
    }
}
