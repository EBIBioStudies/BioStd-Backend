package uk.ac.ebi.biostd.exporter.persistence.dao;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.persistence.AuxQueries;

@Component
@AllArgsConstructor
public class MetricsDao {

    private final NamedParameterJdbcTemplate template;
    private final AuxQueries auxQueries;

    public long getTotalFileSize() {
        return template.queryForObject(auxQueries.getSubmissionsTotalFileSize(), emptyMap(), Long.class);
    }

    public int getSubFilesCount(String accNo) {
        return template.queryForObject(auxQueries.getSubmissionFiles(), singletonMap("accNo", accNo), Integer.class);
    }

    public long getSubFilesSize(String accNo) {
        Long sum = template
                .queryForObject(auxQueries.getSubmissionFilesSize(), singletonMap("accNo", accNo), Long.class);
        return sum == null ? 0 : sum;
    }
}
