package uk.ac.ebi.biostd.test.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;

public class JsonComparator extends DefaultComparator {

    private final Set<String> ignoredFields;

    public JsonComparator(JSONCompareMode mode, String... ignoredFields) {
        super(mode);
        this.ignoredFields = new HashSet<>(Arrays.asList(ignoredFields));
    }

    @Override
    public void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result)
            throws JSONException {

        if (ignoredFields.contains(prefix)) {
            return;
        }

        super.compareValues(prefix, expectedValue, actualValue, result);
    }
}
