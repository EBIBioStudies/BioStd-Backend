package uk.ac.ebi.biostd.webapp.application.persitence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessTag;

public interface AccessTagsRepository extends JpaRepository<AccessTag, Long> {

}
