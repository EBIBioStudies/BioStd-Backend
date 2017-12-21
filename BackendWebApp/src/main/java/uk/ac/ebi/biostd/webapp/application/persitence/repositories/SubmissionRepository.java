package uk.ac.ebi.biostd.webapp.application.persitence.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessTag;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long>, JpaSpecificationExecutor {

    List<Submission> findByRootSectionTypeAndAccessTagInAndVersionGreaterThan(
            String type, List<AccessTag> accessTags, int version);

    default List<Submission> findProjectsByAccessTags(List<AccessTag> accessTags) {
        return findByRootSectionTypeAndAccessTagInAndVersionGreaterThan("Project", accessTags, 0);
    }
}
