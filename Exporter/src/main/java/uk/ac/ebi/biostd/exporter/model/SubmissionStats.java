package uk.ac.ebi.biostd.exporter.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubmissionStats {
    private int filesCount;
    private long filesSize;
}
