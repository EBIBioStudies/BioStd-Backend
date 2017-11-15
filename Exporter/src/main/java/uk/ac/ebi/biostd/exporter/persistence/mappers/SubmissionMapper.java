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
        Submission submission = new Submission();
        submission.setId(resultSet.getLong("id"));
        submission.setAccno(resultSet.getString("accno"));
        submission.setTitle(resultSet.getString("title"));
        submission.setSecretKey(resultSet.getString("secretKey"));
        submission.setRelPath(resultSet.getString("relPath"));
        submission.setRTime(resultSet.getString("rTime"));
        submission.setCTime(resultSet.getString("cTime"));
        submission.setMTime(resultSet.getString("mTime"));
        submission.setRootSection_id(resultSet.getLong("rootSection_id"));
        submission.setOwner_id(resultSet.getLong("owner_id"));

        return submission;
    }
}
