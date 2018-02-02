package uk.ac.ebi.biostd.exporter.jobs.full.json;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.record.Batch;
import org.easybatch.core.record.Record;
import org.easybatch.core.writer.RecordWriter;

@Slf4j
public class JsonBufferedFileWriter implements RecordWriter {

    private static final String DATA_SEPARATOR = ",";
    private final String fileName;
    private final String tempFileName;

    private BufferedWriter bw;
    private AtomicBoolean writeSeparator;

    public JsonBufferedFileWriter(String fileName) {
        this.fileName = fileName;
        this.tempFileName = fileName + "_tmp";
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

        copy(Paths.get(tempFileName), Paths.get(fileName), REPLACE_EXISTING);
        delete(Paths.get(tempFileName));
    }
}
