package uk.ac.ebi.biostd.exporter.persistence.dao;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.persistence.Queries;

@Component
@AllArgsConstructor
public class StatsDao {
    private static final String VIEW_TYPE = "VIEWS";

    private final Queries queries;
    private final NamedParameterJdbcTemplate template;

    public Integer getViews(String accNo) {
        Integer views;
        Map<String, String> params = ImmutableMap.of("accNo", accNo, "type", VIEW_TYPE);

        try {
            views = template.queryForObject(queries.getSubmissionStatsQuery(), params, Integer.class);
        } catch (EmptyResultDataAccessException exception) {
            views = 0;
        }

        return views;
    }
}
