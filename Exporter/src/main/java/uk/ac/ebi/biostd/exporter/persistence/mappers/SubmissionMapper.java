package uk.ac.ebi.biostd.exporter.persistence.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.model.Submission;

@Component
public class SubmissionMapper implements RowMapper<Submission> {

    @Override
    public Submission mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Submission
            .builder()
            .id(resultSet.getLong("id"))
            .accno(resultSet.getString("accno"))
            .title(resultSet.getString("title"))
            .secretKey(resultSet.getString("secretKey"))
            .relPath(resultSet.getString("relPath"))
            .rTime(resultSet.getString("rTime"))
            .cTime(resultSet.getString("cTime"))
            .mTime(resultSet.getString("mTime"))
            .rootSection_id(resultSet.getLong("rootSection_id"))
            .owner_id(resultSet.getLong("owner_id"))
            .released(resultSet.getBoolean("released"))
            .build();
    }
}
