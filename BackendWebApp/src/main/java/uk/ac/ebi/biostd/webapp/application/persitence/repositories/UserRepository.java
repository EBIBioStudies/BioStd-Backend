package uk.ac.ebi.biostd.webapp.application.persitence.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginOrEmail(String login, String email);
}
