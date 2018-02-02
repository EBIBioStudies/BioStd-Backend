package uk.ac.ebi.biostd.exporter.jobs.full.xml.parsing;

import java.util.List;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.apache.commons.lang3.StringUtils;

public class AccessTagsXmlAdapter extends XmlAdapter<String, List<String>> {

    @Override
    public List<String> unmarshal(String v) throws Exception {
        throw new UnsupportedOperationException("unmarshal is not implemented");
    }

    @Override
    public String marshal(List<String> tags) throws Exception {
        return "~" + StringUtils.join(tags, ";");
    }
}
