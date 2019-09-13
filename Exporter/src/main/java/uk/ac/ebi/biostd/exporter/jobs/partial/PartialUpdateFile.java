package uk.ac.ebi.biostd.exporter.jobs.partial;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.biostd.exporter.model.ExecutionStats;
import uk.ac.ebi.biostd.exporter.model.Submission;

@Data
@Builder
class PartialUpdateFile {

    private final List<Submission> submissions;
    private final List<String> updatedSubmissions;
    private final ExecutionStats stats;
}
