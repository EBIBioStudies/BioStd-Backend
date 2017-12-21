package uk.ac.ebi.biostd.webapp.application.common.utils;


import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PlainFileFormat {

    private static final String KEY_VALUE_SEPARATOR = ": ";
    private static final String LINE_BREAK = "\n";

    public String asPlainFile(Map<String, String> fileData) {
        StringBuilder file = new StringBuilder();
        fileData.entrySet().forEach(entry -> file.append(entry.getKey())
                .append(KEY_VALUE_SEPARATOR)
                .append(entry.getValue())
                .append(LINE_BREAK));

        return file.toString();
    }
}
