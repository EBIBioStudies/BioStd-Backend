package uk.ac.ebi.biostd.exporter.jobs.pmc.job;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import lombok.SneakyThrows;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Record;
import org.easybatch.core.record.StringRecord;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.pmc.model.Link;

@Component
public class PmcXmlProcessor implements RecordProcessor<Record<Link>, Record<String>> {

    private final Marshaller marshaller;

    @SneakyThrows
    PmcXmlProcessor() {
        JAXBContext jaxbContext = JAXBContext.newInstance(Link.class);
        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
    }

    @Override
    public Record processRecord(Record<Link> record) throws Exception {
        if (record.getPayload() instanceof Link) {
            StringWriter sw = new StringWriter();
            marshaller.marshal(record.getPayload(), sw);
            return new StringRecord(record.getHeader(), sw.toString());
        }

        return record;
    }
}
