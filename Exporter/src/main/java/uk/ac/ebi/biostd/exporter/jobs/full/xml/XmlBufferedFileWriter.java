package uk.ac.ebi.biostd.exporter.jobs.full.xml;

import static uk.ac.ebi.biostd.exporter.jobs.full.json.JsonBufferedFileWriter.TEMP_FILE_FORMAT;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.record.Batch;
import org.easybatch.core.record.Record;
import org.easybatch.core.writer.RecordWriter;

@Slf4j
public class XmlBufferedFileWriter implements RecordWriter {

    private static final String DATA_SEPARATOR = "\n";
    private final String fileName;

    private BufferedWriter bw;

    @SneakyThrows
    public XmlBufferedFileWriter(String fileName) {
        this.fileName = String.format(TEMP_FILE_FORMAT, fileName);
    }

    @Override
    public void open() throws Exception {
        Files.deleteIfExists(Paths.get(fileName));
        bw = new BufferedWriter(new FileWriter(fileName));
        bw.write("<pmdocument>\n<submissions>\n");
        bw.flush();
    }

    @Override
    public void writeRecords(Batch batch) throws Exception {
        for (Record record : batch) {
            bw.write(record.getPayload().toString());
            bw.write(DATA_SEPARATOR);
        }

        bw.flush();
    }

    @Override
    public void close() throws Exception {
        bw.write("</submissions>\n</pmdocument>\n");
        bw.close();
    }
}
