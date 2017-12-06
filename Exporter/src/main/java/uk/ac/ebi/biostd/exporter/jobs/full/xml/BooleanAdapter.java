package uk.ac.ebi.biostd.exporter.jobs.full.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class BooleanAdapter extends XmlAdapter<Boolean, Boolean> {

    @Override
    public Boolean unmarshal(Boolean v) throws Exception {
        return Boolean.TRUE.equals(v);
    }

    @Override
    public Boolean marshal(Boolean value) throws Exception {
        if (value == null || !value) {
            return null;
        }

        return Boolean.TRUE;
    }
}
