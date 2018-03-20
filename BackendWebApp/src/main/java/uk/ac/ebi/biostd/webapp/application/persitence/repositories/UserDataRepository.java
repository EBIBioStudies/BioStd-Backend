package uk.ac.ebi.biostd.webapp.application.persitence.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserData;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserDataId;

public interface UserDataRepository extends JpaRepository<UserData, UserDataId> {

    List<UserData> findByUserDataIdUserIdAndTopic(long userId, String topic);

    List<UserData> findByUserDataIdUserId(long userId);

    Optional<UserData> findByUserDataId(UserDataId userDataId);
}
