package uk.ac.ebi.biostd.exporter.jobs.pmc.job;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.common.api.DataWriter;

@Component
public class XmlLinksWriter implements DataWriter<String> {

    @Override
    public InputStream getInputStream(List<Record<String>> records) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<links> \n");
        records.forEach(record -> stringBuilder.append(record.getPayload()).append("\n"));
        stringBuilder.append("</links>");

        return new ByteArrayInputStream(stringBuilder.toString().getBytes());
    }
}
