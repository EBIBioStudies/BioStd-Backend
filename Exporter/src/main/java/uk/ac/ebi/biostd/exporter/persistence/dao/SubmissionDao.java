package uk.ac.ebi.biostd.exporter.persistence.dao;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.jobs.full.FullExportJobProperties;
import uk.ac.ebi.biostd.exporter.model.Attribute;
import uk.ac.ebi.biostd.exporter.model.Submission;
import uk.ac.ebi.biostd.exporter.persistence.Queries;
import uk.ac.ebi.biostd.exporter.persistence.mappers.AttributeMapper;
import uk.ac.ebi.biostd.exporter.persistence.mappers.SubmissionMapper;

@Component
@AllArgsConstructor
@Slf4j
public class SubmissionDao {

    private final FullExportJobProperties configProperties;
    private final Queries queries;
    private final NamedParameterJdbcTemplate template;
    private final AttributeMapper attributeMapper;
    private final SubmissionMapper submissionMapper;

    public List<Submission> getUpdatedSubmissions(long syncTime) {
        return template
                .query(queries.getUpdatedSubmissionsQuery(), singletonMap("sync_time", syncTime), submissionMapper);
    }

    public List<String> getAccessTags(long submissionId) {
        return template.queryForList(queries.getSubmissionAccessTagQuery(), singletonMap("submissionId", submissionId),
                String.class);
    }

    public List<Attribute> getAttributes(long submissionId) {
        return template.query(
                queries.getSubmissionAttributesQuery(),
                singletonMap("submissionId", submissionId),
                attributeMapper);
    }

    public String getUserEmail(long userId) {
        return template.queryForObject(queries.getUserEmailQuery(), singletonMap("user_id", userId), String.class);
    }

    public List<Submission> getSubmissions() {
        String query = queries.getSubmissionsQuery() + configProperties.getQueryModified();
        return template.query(query, emptyMap(), submissionMapper);
    }

    public List<Submission> getPmcSubmissions() {
        return template.query(queries.getSubmissionsPmcQuery(), emptyMap(), submissionMapper);
    }

    public String getPublicationId(long submissionId) {
        return template.queryForObject(queries.getSubmissionPublicationQuery(),
                singletonMap("submissionId", submissionId), String.class);
    }
}
