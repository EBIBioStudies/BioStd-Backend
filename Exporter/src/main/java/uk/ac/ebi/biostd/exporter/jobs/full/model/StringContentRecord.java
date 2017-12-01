package uk.ac.ebi.biostd.exporter.jobs.full.model;

import lombok.AllArgsConstructor;
import org.easybatch.core.record.Header;
import org.easybatch.core.record.Record;

@AllArgsConstructor
public class StringContentRecord implements Record<String> {

    private final Header header;
    private final String content;

    @Override
    public Header getHeader() {
        return header;
    }

    @Override
    public String getPayload() {
        return content;
    }
}
