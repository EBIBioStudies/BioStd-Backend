package uk.ac.ebi.biostd.webapp.application.security.service;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.webapp.application.persitence.aux.AuxInfo;
import uk.ac.ebi.biostd.webapp.application.persitence.aux.Parameter;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.SecurityToken;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.Submission;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.AccessPermissionRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.SubmissionRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.TokenRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.UserRepository;
import uk.ac.ebi.biostd.webapp.application.security.common.ISecurityService;
import uk.ac.ebi.biostd.webapp.application.security.common.SecurityAccessException;
import uk.ac.ebi.biostd.webapp.application.security.entities.LoginRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignUpRequest;

@Service
@Transactional
@AllArgsConstructor
public class SecurityService implements ISecurityService {

    private final UserRepository userRepository;
    private final AccessPermissionRepository permissionRepository;
    private final SubmissionRepository submissionRepository;
    private final TokenRepository tokenRepository;
    private final SecurityUtil securityUtil;

    @Override
    public List<Submission> getAllowedProjects(long userId, AccessType accessType) {
        return permissionRepository.findByUserIdAndAccessType(userId, accessType).stream()
                .map(AccessPermission::getAccessTag)
                .map(submissionRepository::findProjectByAccessTag)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    @Override
    public User getPermissions(LoginRequest loginInfo) {
        String login = loginInfo.getLogin();
        String hash = loginInfo.getHash();

        Optional<User> user = userRepository.findByLoginOrEmail(login, login);

        if (!user.isPresent()) {
            throw new SecurityAccessException(format("Could find an user register with email or login '%s'", login));
        }

        if (!securityUtil.checkHash(hash, user.get().getPasswordDigest())) {
            throw new SecurityAccessException(format("Given hash '%s' do not match for user '%s'", hash, login));
        }

        return user.get();
    }

    @Override
    public String signIn(String login, String password) {
        Optional<User> user = userRepository.findByLoginOrEmail(login, login);

        if (!user.isPresent()) {
            throw new SecurityAccessException(format("Could find an user register with email or login '%s'", login));
        }

        if (!securityUtil.checkPassword(user.get().getPasswordDigest(), password)) {
            throw new SecurityAccessException(
                    format("Given password '%s' do not match for user '%s'", password, login));
        }

        return securityUtil.createToken(user.get());
    }

    @Override
    public void signOut(String securityKey) {
        Optional<SecurityToken> token = tokenRepository.findById(securityKey);
        if (token.isPresent()) {
            token.get().setInvalidationDate(OffsetDateTime.now(Clock.systemUTC()));
            tokenRepository.save(token.get());
        }
    }

    @Override
    public void addUser(SignUpRequest signUpRequest) {
        User user = User.builder()
                .email(signUpRequest.getEmail())
                .fullName(signUpRequest.getUsername())
                .auxProfileInfo(createAuxInfo(signUpRequest.getAux()))
                .active(false)
                .activationKey(UUID.randomUUID().toString()).build();

        userRepository.save(user.withPendingActivation(signUpRequest.getActivationURL()));
    }

    private AuxInfo createAuxInfo(List<String> dataList) {
        AuxInfo auxInfo = new AuxInfo();
        auxInfo.setParameters(dataList.stream()
                .map(data -> data.split(":"))
                .map(pairs -> new Parameter(pairs))
                .collect(toList()));
        return auxInfo;
    }

    @Override
    public boolean activate(String activationKey) {
        boolean activated = false;
        Optional<User> optionalUser = userRepository.findByActivationKey(activationKey);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setActivationKey(null);
            user.setActive(true);
            userRepository.save(user);
            activated = true;
        }

        return activated;
    }

    @Override
    public User getUserByKey(String key) {
        TokenUser tokenUser = securityUtil.fromToken(key);
        return userRepository.findOne(tokenUser.getId());
    }

    @Override
    public boolean resetPassword(String key, String password) {
        boolean activated = false;
        Optional<User> optionalUser = userRepository.findByActivationKey(key);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setActivationKey(null);
            user.setPasswordDigest(securityUtil.getPasswordDigest(password));
            userRepository.save(user);
            activated = true;
        }

        return activated;
    }

    @Override
    public void resetPasswordRequest(String email, String activationUrl) {
        Optional<User> optionalUser = userRepository.findByLoginOrEmail(email, email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setActivationKey(null);
            user.setActivationKey(UUID.randomUUID().toString());
            userRepository.save(user.withResetPassword(activationUrl));
        }
    }
}
