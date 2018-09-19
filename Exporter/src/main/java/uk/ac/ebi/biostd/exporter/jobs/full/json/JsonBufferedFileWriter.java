package uk.ac.ebi.biostd.exporter.jobs.full.json;

import static java.lang.String.format;
import static java.nio.file.Files.deleteIfExists;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.record.Batch;
import org.easybatch.core.record.Record;
import org.easybatch.core.writer.RecordWriter;


/**
 * Writes submission representation into export file.
 */
@Slf4j
public class JsonBufferedFileWriter implements RecordWriter {

    public static final String TEMP_FILE_FORMAT = "%s_tmp";

    private static final String DATA_SEPARATOR = ",";
    private final String tempFileName;

    private BufferedWriter bw;
    private AtomicBoolean writeSeparator;

    public JsonBufferedFileWriter(String fileName) {
        this.tempFileName = format(TEMP_FILE_FORMAT, fileName);
    }

    @Override
    public void open() throws Exception {
        writeSeparator = new AtomicBoolean(false);

        deleteIfExists(Paths.get(tempFileName));
        bw = new BufferedWriter(new FileWriter(tempFileName));
        bw.write("{\n \"submissions\" :[\n");
        bw.flush();
    }

    @Override
    public void writeRecords(Batch batch) throws Exception {
        for (Record record : batch) {
            if (writeSeparator.getAndSet(true)) {
                bw.write(DATA_SEPARATOR);
            }
            bw.write(record.getPayload().toString());
        }

        bw.flush();
    }

    @Override
    public void close() throws Exception {
        bw.write("]");
        bw.close();
    }
}
