package uk.ac.ebi.biostd.exporter.persistence.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.model.SubmissionStats;

@Component
public class StatsSubmissionMapper implements RowMapper<SubmissionStats> {
    @Override
    public SubmissionStats mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        SubmissionStats stats = new SubmissionStats();
        stats.setFilesCount(resultSet.getInt("filesCount"));
        stats.setFilesSize(resultSet.getLong("submissionFilesSize"));
        stats.setRefFilesCount(resultSet.getInt("refFilesCount"));
        stats.setRefFilesSize(resultSet.getLong("refFilesSize"));

        return stats;
    }
}
