package uk.ac.ebi.biostd.webapp.application.persitence.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessTag;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long>, JpaSpecificationExecutor {

    String PROJECT_TYPE = "Project";

    Optional<Submission> findByRootSectionTypeAndAccessTagAndVersionGreaterThan(
            String type, AccessTag accessTag, int version);

    default Optional<Submission> findProjectByAccessTag(AccessTag accessTag) {
        return findByRootSectionTypeAndAccessTagAndVersionGreaterThan(PROJECT_TYPE, accessTag, 0);
    }
}
