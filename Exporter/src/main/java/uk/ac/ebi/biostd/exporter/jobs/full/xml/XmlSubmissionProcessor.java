package uk.ac.ebi.biostd.exporter.jobs.full.xml;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import lombok.SneakyThrows;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Record;
import org.easybatch.core.record.StringRecord;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.model.Submission;

@Component
public class XmlSubmissionProcessor implements RecordProcessor<Record<Submission>, Record<String>> {

    private final Marshaller marshaller;

    @SneakyThrows
    XmlSubmissionProcessor() {
        JAXBContext jaxbContext = JAXBContext.newInstance(Submission.class);
        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
    }

    @Override
    public Record processRecord(Record<Submission> record) throws Exception {
        if (record.getPayload() instanceof Submission) {
            StringWriter sw = new StringWriter();
            marshaller.marshal(record.getPayload(), sw);
            return new StringRecord(record.getHeader(), sw.toString());
        }

        return record;
    }
}
