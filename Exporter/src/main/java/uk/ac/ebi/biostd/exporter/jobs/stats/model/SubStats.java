package uk.ac.ebi.biostd.exporter.jobs.stats.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class SubStats {

    private String accNo;
    private final long subFileSize;
    private final int files;
    private final long filesSize;
}
