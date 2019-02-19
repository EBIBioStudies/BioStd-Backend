package uk.ac.ebi.biostd.exporter.persistence.dao;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.configuration.ExporterGeneralProperties;
import uk.ac.ebi.biostd.exporter.jobs.stats.StatsProperties;
import uk.ac.ebi.biostd.exporter.model.Attribute;
import uk.ac.ebi.biostd.exporter.model.Submission;
import uk.ac.ebi.biostd.exporter.persistence.Queries;
import uk.ac.ebi.biostd.exporter.persistence.mappers.AttributeMapper;
import uk.ac.ebi.biostd.exporter.persistence.mappers.DetailedSubmissionMapper;
import uk.ac.ebi.biostd.exporter.persistence.mappers.LibFileSubmissionMapper;
import uk.ac.ebi.biostd.exporter.persistence.mappers.StatsSubmissionMapper;
import uk.ac.ebi.biostd.exporter.persistence.mappers.SubmissionMapper;
import uk.ac.ebi.biostd.exporter.persistence.model.SubAndUserInfo;

@Slf4j
@Component
@AllArgsConstructor
public class SubmissionDao {
    private final Queries queries;
    private final AttributeMapper attributeMapper;
    private final StatsProperties statsProperties;
    private final SubmissionMapper submissionMapper;
    private final NamedParameterJdbcTemplate template;
    private final ExporterGeneralProperties properties;
    private final StatsSubmissionMapper statsSubmissionMapper;
    private final LibFileSubmissionMapper libFileSubmissionMapper;
    private final DetailedSubmissionMapper detailedSubmissionMapper;

    public void releaseSubmission(long submissionId) {
        template.update(queries.getReleaseSubmission(), ImmutableMap.of("subId", submissionId));
        template.update(queries.getAddPublicAccessTag(), ImmutableMap.of("subId", submissionId));
    }

    public List<SubAndUserInfo> getPendingToReleaseSub(long epochSecondsFrom, long epochSecondsTo) {
        return template.query(
                queries.getPendingRelease(),
                ImmutableMap.of("from", epochSecondsFrom, "to", epochSecondsTo),
                new BeanPropertyRowMapper<>(SubAndUserInfo.class));
    }

    public List<String> getPublicSubmissionsPaths() {
        return template.queryForList(queries.getPublicSubmissions(), emptyMap(), String.class);
    }

    public List<Submission> getUpdatedSubmissions(long syncTime) {
        return template.query(
                queries.getUpdatedSubmissionsQuery(),
                ImmutableMap.of("sync_time", syncTime, "libFileStudies", properties.getLibFileStudies()),
                libFileSubmissionMapper);
    }

    public Submission getSubmissionByAccNo(String accNo) {
        return template.queryForObject(
                queries.getSubmissionsQueryByAccNo(),
                ImmutableMap.of("accno", accNo, "libFileStudies", properties.getLibFileStudies()),
                libFileSubmissionMapper);
    }

    public List<String> getDeletedSubmissions(long syncTime) {
        return template.queryForList(
                queries.getDeletedSubmissionsQuery(), singletonMap("sync_time", syncTime), String.class);
    }

    public List<String> getAccessTags(long submissionId) {
        return template.queryForList(
                queries.getSubmissionAccessTagQuery(), singletonMap("submissionId", submissionId), String.class);
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
        return template.query(
                queries.getSubmissionsQuery(),
                singletonMap("libFileStudies", properties.getLibFileStudies()),
                detailedSubmissionMapper);
    }

    public List<Submission> getStatsSubmissions() {
        return template.query(
                queries.getSubmissionsStatsQuery(),
                singletonMap("imagingProjects", statsProperties.getImagingProjects()),
                statsSubmissionMapper);
    }

    public List<Submission> getPmcSubmissions() {
        return template.query(queries.getSubmissionsPmcQuery(), emptyMap(), submissionMapper);
    }
}
