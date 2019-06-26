package uk.ac.ebi.biostd.exporter.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubmissionFileListStats {
    private int refFilesCount;
    private long refFilesSize;
}
