package uk.ac.ebi.biostd.webapp.application.persitence.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessTag;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long>, JpaSpecificationExecutor {

    List<Submission> findByRootSectionTypeAndAccessTagInAndVersionGreaterThan(
            String type, List<AccessTag> accessTags, int version);

    Optional<Submission> findByRootSectionTypeAndAccessTagAndVersionGreaterThan(
            String type, AccessTag accessTag, int version);

    Optional<Submission> findByRootSectionTypeAndAccNoAndVersionGreaterThan(
            String type, String accno, int version);

    default List<Submission> findProjectsByAccessTags(List<AccessTag> accessTags) {
        return findByRootSectionTypeAndAccessTagInAndVersionGreaterThan("Project", accessTags, 0);
    }

    default Optional<Submission> findProjectByAccessTag(AccessTag accessTag) {
        return findByRootSectionTypeAndAccessTagAndVersionGreaterThan("Project", accessTag, 0);
    }

    default Optional<Submission> findProjectByAccession(String accno) {
        return findByRootSectionTypeAndAccNoAndVersionGreaterThan("Project", accno, 0);
    }
}
