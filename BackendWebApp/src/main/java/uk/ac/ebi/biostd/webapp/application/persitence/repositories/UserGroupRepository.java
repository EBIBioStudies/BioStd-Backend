package uk.ac.ebi.biostd.webapp.application.persitence.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserGroup;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    Optional<UserGroup> findByNameAndUsersContains(String groupName, User user);
}
