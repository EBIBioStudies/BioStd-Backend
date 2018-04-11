package uk.ac.ebi.biostd.webapp.application.domain.services;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserData;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserDataId;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.UserDataRepository;

@AllArgsConstructor
@Service
public class UserDataService {

    private final UserDataRepository userDataRepository;

    public void deleteModifiedSubmission(long userId, String key) {
        userDataRepository.deleteById(new UserDataId(key, userId));
    }

    public List<UserData> findAllByUserAndTopic(long userId, String topic) {
        return Strings.isNullOrEmpty(topic) ?
                userDataRepository.findByUserDataIdUserId(userId) :
                userDataRepository.findByUserDataIdUserIdAndTopic(userId, topic);
    }

    public Optional<UserData> findByUserAndKey(long userId, String key) {
        return userDataRepository.findByUserDataId(new UserDataId(key, userId));
    }

    public UserData update(long userId, String key, String data, String topic) {
        UserDataId dataKey = new UserDataId(key, userId);
        UserData userData = userDataRepository.findByUserDataId(dataKey).orElse(new UserData(dataKey));
        userData.setData(data);
        userData.setTopic(topic);
        return userDataRepository.save(userData);
    }
}
