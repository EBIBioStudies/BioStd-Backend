package uk.ac.ebi.biostd.exporter.jobs.full.xml;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.delete;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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
public class BufferedXmlFileWriter implements RecordWriter {

    private static final String DATA_SEPARATOR = "\n";
    private final String fileName;
    private final String tempFileName;

    private BufferedWriter bw;

    @SneakyThrows
    public BufferedXmlFileWriter(String fileName) {
        this.fileName = fileName;
        this.tempFileName = fileName + "_tmp";
    }

    @Override
    public void open() throws Exception {
        Files.deleteIfExists(Paths.get(tempFileName));
        bw = new BufferedWriter(new FileWriter(tempFileName));
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

        copy(Paths.get(tempFileName), Paths.get(fileName), REPLACE_EXISTING);
        delete(Paths.get(tempFileName));
    }
}
