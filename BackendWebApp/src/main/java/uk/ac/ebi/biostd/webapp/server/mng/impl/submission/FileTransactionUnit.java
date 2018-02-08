package uk.ac.ebi.biostd.webapp.server.mng.impl.submission;

import java.nio.file.Path;

class FileTransactionUnit {

    Path submissionPath;
    Path historyPath;
    Path submissionPathTmp;
    Path historyPathTmp;
    SubmissionDirState state;
}
