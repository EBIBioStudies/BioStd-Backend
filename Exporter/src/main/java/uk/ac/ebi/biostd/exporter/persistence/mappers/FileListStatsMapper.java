package uk.ac.ebi.biostd.exporter.persistence.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.model.SubmissionFileListStats;

@Component
public class FileListStatsMapper implements RowMapper<SubmissionFileListStats> {
    @Override
    public SubmissionFileListStats mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        SubmissionFileListStats stats = new SubmissionFileListStats();
        stats.setRefFilesCount(resultSet.getInt("refFilesCount"));
        stats.setRefFilesSize(resultSet.getLong("refFilesSize"));

        return stats;
    }
}
