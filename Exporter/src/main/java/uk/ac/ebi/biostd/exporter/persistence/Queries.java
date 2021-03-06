package uk.ac.ebi.biostd.exporter.persistence;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Declares the different queries used to obtain submissions information.
 */
@Data
@Component
@ConfigurationProperties
public class Queries {

    private String submissionsQuery;
    private String simpleSubmissionsQuery;
    private String submissionsStatsQuery;
    private String submissionFileListStatsQuery;
    private String submissionsPmcQuery;
    private String fileAttributesQuery;
    private String linkAttributesQuery;
    private String linksBySectionQuery;
    private String sectionAttributesQuery;
    private String sectionFilesQuery;
    private String sectionFilesCountQuery;
    private String sectionReferencedFilesCountQuery;
    private String sectionSectionsQuery;
    private String sectionByIdQuery;
    private String submissionAccessTagQuery;
    private String submissionAttributesQuery;
    private String singleSubmissionQuery;
    private String userEmailQuery;
    private String updatedSubmissionsQuery;
    private String submissionsQueryByAccNo;
    private String deletedSubmissionsQuery;
    private String submissionPublicationQuery;
    private String publicSubmissions;
    private String publicSubmissionsByAccNo;
    private String userDropboxes;
    private String pendingRelease;
    private String releaseSubmission;
    private String addPublicAccessTag;
    private String submissionStatsQuery;
}
