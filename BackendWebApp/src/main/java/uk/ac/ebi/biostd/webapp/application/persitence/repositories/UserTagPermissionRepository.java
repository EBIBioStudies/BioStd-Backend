package uk.ac.ebi.biostd.webapp.application.persitence.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserTagPermission;

public interface UserTagPermissionRepository extends JpaRepository<UserTagPermission, Long> {

    List<UserTagPermission> findByUserIdAndPermissionName(long userId, String permissionName);
}
