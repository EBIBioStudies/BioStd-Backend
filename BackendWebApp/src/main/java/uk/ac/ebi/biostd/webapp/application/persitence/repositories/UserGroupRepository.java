package uk.ac.ebi.biostd.webapp.application.persitence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserGroup;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

}
