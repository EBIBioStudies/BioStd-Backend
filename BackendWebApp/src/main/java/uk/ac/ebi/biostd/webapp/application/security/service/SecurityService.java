package uk.ac.ebi.biostd.webapp.application.security.service;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.webapp.application.persitence.aux.AuxInfo;
import uk.ac.ebi.biostd.webapp.application.persitence.aux.Parameter;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessTag;
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
import uk.ac.ebi.biostd.webapp.application.security.rest.model.UserData;

@Service
@Transactional
@AllArgsConstructor
public class SecurityService implements ISecurityService {

    private final UserRepository userRepository;
    private final AccessPermissionRepository permissionsRepository;
    private final SubmissionRepository submissionRepository;
    private final TokenRepository tokenRepository;
    private final SecurityUtil securityUtil;

    @Override
    public List<Submission> getAllowedProjects(long userId, AccessType accessType) {
        return permissionsRepository.findByUserIdAndAccessType(userId, accessType).stream()
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
    public UserData signIn(String login, String password) {
        Optional<User> user = userRepository.findByLoginOrEmail(login, login);

        if (!user.isPresent()) {
            throw new SecurityAccessException(format("Could find an user register with email or login '%s'", login));
        }

        if (!securityUtil.checkPassword(user.get().getPasswordDigest(), password)) {
            throw new SecurityAccessException(
                    format("Given password '%s' do not match for user '%s'", password, login));
        }

        String token = securityUtil.createToken(user.get());
        return new UserData(token, user.get());
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
        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setFullName(signUpRequest.getUsername());
        user.setAuxProfileInfo(createAuxInfo(signUpRequest.getAux()));
        user.setActive(false);
        user.setActivationKey(UUID.randomUUID().toString());
        user.setPasswordDigest(securityUtil.getPasswordDigest(signUpRequest.getPassword()));

        userRepository.save(user.withPendingActivation(signUpRequest.getActivationURL()));
    }

    private AuxInfo createAuxInfo(List<String> dataList) {
        AuxInfo auxInfo = new AuxInfo();
        auxInfo.setParameters(dataList.stream()
                .map(data -> data.split(":"))
                .map(Parameter::new)
                .collect(toList()));
        return auxInfo;
    }

    @Override
    public void activate(String activationKey) {
        Optional<User> optionalUser = userRepository.findByActivationKey(activationKey);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setActivationKey(null);
            user.setActive(true);
            userRepository.save(user);
        }
    }

    @Override
    public User getUserByKey(String key) {
        TokenUser tokenUser = securityUtil.fromToken(key);
        return userRepository.findOne(tokenUser.getId());
    }

    @Override
    public void resetPassword(String key, String password) {
        Optional<User> optionalUser = userRepository.findByActivationKey(key);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setActivationKey(null);
            user.setPasswordDigest(securityUtil.getPasswordDigest(password));
            userRepository.save(user);
        }
    }

    @Override
    public void resetPasswordRequest(String email, String activationUrl) {
        Optional<User> optionalUser = userRepository.findByLoginOrEmail(email, email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setActivationKey(UUID.randomUUID().toString());
            userRepository.save(user.withResetPasswordRequest(activationUrl));
        }
    }

    @Override
    public boolean hasPermission(long submissionId, long userId, AccessType accessType) {
        User user = userRepository.findOne(userId);
        Submission submission = submissionRepository.findOne(submissionId);

        boolean isAuthor = submission.getOwnerId() == userId;
        boolean isPublic = isPublicSubmission(submission.getAccessTag());
        boolean isSuperUser = user.isSuperuser();
        boolean hasAccessTag = permissionsRepository
                .existsByAccessTagInAndAccessType(submission.getAccessTag(), accessType);

        switch (accessType) {
            case READ:
            case SUBMIT:
                return isPublic || isAuthor || isSuperUser;
            case ATTACH:
                return hasAccessTag || isSuperUser;
            case UPDATE:
            case DELETE:
                return isAuthor || isSuperUser;
        }

        throw new IllegalStateException("Not supported access type ");
    }

    @Override
    public int getUsersCount() {
        return Math.toIntExact(userRepository.count());
    }

    private boolean isPublicSubmission(Set<AccessTag> accessTags) {
        return accessTags.stream().anyMatch(tag -> tag.getName().equals("Public"));
    }
}
