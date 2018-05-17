package uk.ac.ebi.biostd.webapp.application.persitence.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long>, JpaSpecificationExecutor<Submission> {

    String PROJECT_TYPE = "Project";

    List<Submission> findByRootSectionTypeAndAccessTagIdInAndVersionGreaterThan(
            String type, List<Long> accessTags, int version);

    default List<Submission> findProjectByAccessTagIdIn(List<Long> accessTags) {
        return findByRootSectionTypeAndAccessTagIdInAndVersionGreaterThan(PROJECT_TYPE, accessTags, 0);
    }
}
