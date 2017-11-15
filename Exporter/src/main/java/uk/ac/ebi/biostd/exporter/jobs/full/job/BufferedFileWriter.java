package uk.ac.ebi.biostd.exporter.jobs.full.job;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.record.Batch;
import org.easybatch.core.record.Record;
import org.easybatch.core.writer.RecordWriter;

@Slf4j
public class BufferedFileWriter implements RecordWriter {

    private static final String DATA_SEPARATOR = ",";
    private final String fileName;


    private BufferedWriter bw;
    private AtomicBoolean writeSeparator;

    public BufferedFileWriter(String fileName) throws IOException {
        this.fileName = fileName;
    }

    @Override
    public void open() throws Exception {
        writeSeparator = new AtomicBoolean(false);

        Files.deleteIfExists(Paths.get(fileName));
        bw = new BufferedWriter(new FileWriter(fileName));
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
