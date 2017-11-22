package uk.ac.ebi.biostd.backend.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Builder;

@Builder
public class PlainFileParser {

    public static final String LINE_BREAKS_SEPARATOR = "[\\r\\n]+";
    public static final String SEMICOLON_SEPARATOR = ":";

    private final String lineSeparator;
    private final String valuesSeparator;

    public List<Triplet> parseFile(String fileContent) {
        List<Triplet> triplets = new ArrayList<>();

        for (String line : fileContent.split(LINE_BREAKS_SEPARATOR)) {

            String[] values = line.split(SEMICOLON_SEPARATOR);

            if (values.length > 1) {
                triplets.add(new Triplet(values[0], Arrays.copyOfRange(values, 1, values.length)));
            }

            if (values.length == 1) {
                triplets.add(new Triplet(values[0]));
            }
        }

        return triplets;
    }
}
