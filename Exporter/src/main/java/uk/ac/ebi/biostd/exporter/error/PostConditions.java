package uk.ac.ebi.biostd.exporter.error;

public class PostConditions {

    public static void checkOutput(boolean expression, String message) {
        if (!expression) {
            throw new IllegalOutputException(message);
        }
    }
}
