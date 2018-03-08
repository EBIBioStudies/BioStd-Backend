package uk.ac.ebi.biostd.webapp.application.persitence.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.SecurityToken;

public interface TokenRepository extends JpaRepository<SecurityToken, String> {

    Optional<SecurityToken> findById(String token);
}
