package uk.ac.ebi.biostd.test.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.w3c.dom.Attr;
import org.xmlunit.util.Predicate;

public class XmlAttributeFilter implements Predicate<Attr> {

    private final Set<String> ignoredFields;

    public XmlAttributeFilter(String... ignoredFields) {
        this.ignoredFields = new HashSet<>(Arrays.asList(ignoredFields));
    }

    @Override
    public boolean test(Attr attr) {
        return ignoredFields.contains(attr.getName());
    }
}
