package uk.ac.ebi.biostd.out;

import uk.ac.ebi.biostd.out.json.JSONFormatter;
import uk.ac.ebi.biostd.out.pageml.PageMLFormatter;

/**
 * Created by andrew on 27/09/2017.
 */
public class FormatterFactory {

    private static final String UNSUPPORTED = "Formatter Not Supported: ";

    private FormatterFactory() {
    }

    public static AbstractFormatter getFormatter(FormatterType type, Appendable o, boolean cut) throws Exception {
        switch (type) {
            case JSON:
                return new JSONFormatter(o, cut);
            case XML:
                return new PageMLFormatter(o, cut);
            default:
                throw new Exception(UNSUPPORTED + type.name());
        }
    }

    public static AbstractFormatter getFormatter(String type, Appendable o, boolean cut) throws Exception {
        try {
            return getFormatter(FormatterType.valueOf(type.toUpperCase()), o, cut);
        } catch (IllegalArgumentException iae) {
            throw new Exception(UNSUPPORTED + type);
        }
    }

}
