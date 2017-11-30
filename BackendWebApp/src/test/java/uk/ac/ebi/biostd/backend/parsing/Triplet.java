package uk.ac.ebi.biostd.backend.parsing;

import lombok.Getter;

@Getter
public class Triplet {

    private final String key;
    private final String[] values;

    public Triplet(String key, String... values) {
        this.key = key;
        this.values = values;
    }

    public boolean getFirstValueAsBoolean() {
        return Boolean.valueOf(values[0].trim());
    }

    public String getFirstValue() {
        return values[0].trim();
    }
}
