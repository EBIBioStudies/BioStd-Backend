package uk.ac.ebi.biostd.webapp.application.rest.dto;

import uk.ac.ebi.biostd.webapp.server.mng.SubmissionManager;

public enum SubmitOperation {
    CREATE(SubmissionManager.Operation.CREATE),
    CREATE_OR_UPDATE(SubmissionManager.Operation.CREATEUPDATE),
    CREATE_OR_OVERRIDE(SubmissionManager.Operation.CREATEOVERRIDE);

    private SubmissionManager.Operation legacyOp;

    SubmitOperation(SubmissionManager.Operation op) {
        this.legacyOp = op;
    }

    public SubmissionManager.Operation toLegacyOp() {
        return legacyOp;
    }
}
