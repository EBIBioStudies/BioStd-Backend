package uk.ac.ebi.biostd.webapp.application.rest.dto;

public enum SubmitStatus {
    OK, FAIL;

    public static SubmitStatus submitStatus(boolean value) {
        return value ? OK : FAIL;
    }
}
