package uk.ac.ebi.biostd.exporter.jobs.common.api;

import java.io.InputStream;
import java.util.List;
import org.easybatch.core.record.Record;

public interface DataWriter<T> {

    public InputStream getInputStream(List<Record<T>> records);
}
