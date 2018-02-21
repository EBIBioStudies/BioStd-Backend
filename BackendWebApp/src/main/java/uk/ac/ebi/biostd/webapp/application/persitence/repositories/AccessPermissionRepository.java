package uk.ac.ebi.biostd.webapp.application.persitence.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType;

public interface AccessPermissionRepository extends JpaRepository<AccessPermission, Long> {

    List<AccessPermission> findByUserIdAndAccessType(long userId, AccessType accessType);
}
