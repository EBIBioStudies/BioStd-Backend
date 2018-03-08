package uk.ac.ebi.biostd.webapp.application.persitence.repositories;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessTag;

public interface AccessPermissionRepository extends JpaRepository<AccessPermission, Long> {

    List<AccessPermission> findByUserIdAndAccessType(long userId, AccessType accessType);

    boolean existsByAccessTagInAndAccessType(Set<AccessTag> accessTags, AccessType accessType);
}
