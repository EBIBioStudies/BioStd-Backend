package uk.ac.ebi.biostd.exporter.jobs.common.easybatch;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.easybatch.core.reader.RecordReader;
import org.easybatch.core.record.GenericRecord;
import org.easybatch.core.record.Header;
import org.easybatch.core.record.Record;

public class DbRecordReader<T> implements RecordReader {

    private final Callable<List<T>> queryFunction;
    private final String dbSchema;

    private List<T> entityList;
    private int submissions;
    private int currentRecord = 0;

    @SneakyThrows
    public DbRecordReader(Callable<List<T>> queryFunction, DataSource dataSource) {
        this.queryFunction = queryFunction;
        dbSchema = dataSource.getConnection().getSchema();
    }

    @Override
    public void open() throws Exception {
        entityList = queryFunction.call();
        submissions = entityList.size();
        currentRecord = 0;
    }

    @Override
    public Record readRecord() {
        if (currentRecord < submissions) {
            GenericRecord<T> record = new GenericRecord<>(new Header(
                    (long) currentRecord,
                    dbSchema,
                    new Date()),
                    entityList.get(currentRecord));
            currentRecord++;
            return record;
        }

        return null;
    }

    @Override
    public void close() {
        entityList = null;
    }

}
