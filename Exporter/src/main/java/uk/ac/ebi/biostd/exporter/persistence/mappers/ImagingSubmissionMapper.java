package uk.ac.ebi.biostd.exporter.persistence.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.model.Submission;

@Component
public class ImagingSubmissionMapper extends SubmissionMapper {
    @Override
    public Submission mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Submission submission = super.mapRow(resultSet, rowNum);
        submission.setImagingSubmission(resultSet.getBoolean("isImagingSubmission"));

        return submission;
    }
}
