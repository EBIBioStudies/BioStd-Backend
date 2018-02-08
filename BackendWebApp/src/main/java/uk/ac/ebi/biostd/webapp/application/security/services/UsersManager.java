package uk.ac.ebi.biostd.webapp.application.security.services;

import com.pri.util.StringUtils;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.UserRepository;

@Component
@AllArgsConstructor
public class UsersManager {

    private final UserRepository userRepository;

    User getUserLogin(String login, String hash) {
        Optional<User> user = userRepository.findByLoginOrEmail(login, login);

        if (!user.isPresent()) {
            throw new SecurityException(String.format("Could find an user register with email or login '%s'", login));
        }

        if (!hashMatch(hash, user.get().getPasswordDigest())) {
            throw new SecurityException(String.format("Given hash '%s' do not match for user '%s'", hash, login));
        }

        return user.get();
    }

    private boolean hashMatch(String hash, byte[] passwordDigest) {
        return hash.equalsIgnoreCase(StringUtils.toHexStr(passwordDigest));
    }
}
