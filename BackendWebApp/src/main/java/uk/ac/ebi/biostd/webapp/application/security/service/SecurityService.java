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
import uk.ac.ebi.biostd.webapp.application.persitence.common.AuxInfo;
import uk.ac.ebi.biostd.webapp.application.persitence.common.Parameter;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessTag;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.SecurityToken;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.Submission;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.AccessPermissionRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.AccessTagsRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.SubmissionRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.TokenRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.UserRepository;
import uk.ac.ebi.biostd.webapp.application.security.entities.LoginRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignUpRequest;
import uk.ac.ebi.biostd.webapp.application.security.error.SecurityException;
import uk.ac.ebi.biostd.webapp.application.security.rest.model.UserData;

@Service
@Transactional
@AllArgsConstructor
public class SecurityService implements ISecurityService {

    public static final String PUBLIC_ACCESS_TAG = "Public";

    private final UserRepository userRepository;
    private final AccessPermissionRepository permissionsRepository;
    private final SubmissionRepository submissionRepository;
    private final TokenRepository tokenRepository;
    private final AccessTagsRepository tagsRepository;

    private final SecurityUtil securityUtil;
    private final MagicFolderUtil magicFolderUtil;

    @Override
    public User getPermissions(LoginRequest loginInfo) {
        String login = loginInfo.getLogin();
        String hash = loginInfo.getHash();

        Optional<User> optionalUser = userRepository.findByLoginOrEmail(login, login);

        if (!optionalUser.isPresent()) {
            throw new SecurityException(format("Could find an user register with email or login '%s'", login));
        }

        if (!securityUtil.checkHash(hash, optionalUser.get().getPasswordDigest())) {
            throw new SecurityException(format("Given hash '%s' do not match for user '%s'", hash, login));
        }

        return optionalUser.get();
    }

    @Override
    public User getUser(long userId) {
        return userRepository.getOne(userId);
    }

    @Override
    public UserData signIn(String login, String password) {
        Optional<User> user = userRepository.findByLoginOrEmail(login, login);

        if (!user.isPresent()) {
            throw new SecurityException(format("Could find an user register with email or login '%s'", login));
        }

        if (!securityUtil.checkPassword(user.get().getPasswordDigest(), password)) {
            throw new SecurityException(format("Given password do not match for user '%s'", login));
        }

        String token = securityUtil.createToken(user.get());
        return new UserData(token, user.get());
    }

    @Override
    public void signOut(String securityKey) {
        SecurityToken securityToken = new SecurityToken();
        securityToken.setInvalidationDate(OffsetDateTime.now(Clock.systemUTC()));
        securityToken.setId(securityKey);
        tokenRepository.save(securityToken);
    }

    @Override
    public void addUser(SignUpRequest signUpRequest) {
        Optional<User> created = userRepository.findByEmail(signUpRequest.getEmail());
        if (created.isPresent()) {
            throw new SecurityException(
                    format("There is already a user register with email %s", created.get().getEmail()));
        }

        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setFullName(signUpRequest.getUsername());
        user.setAuxProfileInfo(createAuxInfo(signUpRequest.getAux()));
        user.setActive(false);
        user.setActivationKey(UUID.randomUUID().toString());
        user.setKeyTime(OffsetDateTime.now().toInstant().toEpochMilli());
        user.setPasswordDigest(securityUtil.getPasswordDigest(signUpRequest.getPassword()));
        user.setSecret(UUID.randomUUID().toString());

        userRepository.save(user.withPendingActivation(signUpRequest.getActivationURL()));
        magicFolderUtil.createUserMagicFolder(user.getId(), user.getSecret());
    }

    @Override
    public User addInactiveUserIfNotExist(String email, String name) {
        return userRepository.findByLoginOrEmail(email, email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setAuxProfileInfo(new AuxInfo());
            user.setSecret(UUID.randomUUID().toString());
            userRepository.save(user);
            magicFolderUtil.createUserMagicFolder(user.getId(), user.getSecret());

            return user;
        });
    }

    @Override
    @Transactional
    public void addPermission(long userId, String domain) {
        AccessTag tag = tagsRepository.findByName(domain).orElseGet(() -> {
            AccessTag accessTag = new AccessTag();
            accessTag.setName(domain);
            return tagsRepository.save(accessTag);
        });

        addPermissionIfNotExist(tag, userId, AccessType.READ);
        addPermissionIfNotExist(tag, userId, AccessType.UPDATE);
        addPermissionIfNotExist(tag, userId, AccessType.SUBMIT);
    }

    private void addPermissionIfNotExist(AccessTag accessTag, long user, AccessType accessType) {
        if (!permissionsRepository.existsByAccessTagNameAndAccessType(accessTag.getName(), accessType)) {
            AccessPermission readPermission = new AccessPermission();
            readPermission.setAccessTag(accessTag);
            readPermission.setUser(userRepository.getOne(user));
            readPermission.setAccessType(accessType);
            permissionsRepository.save(readPermission);
        }
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
    public Optional<User> getUserByKey(String key) {
        Optional<SecurityToken> token = tokenRepository.findById(key);
        if (token.isPresent()) {
            return Optional.empty();
        }

        return securityUtil.fromToken(key).map(tokenUser1 -> userRepository.getOne(tokenUser1.getId()));
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
        User user = userRepository.getOne(userId);
        Submission submission = submissionRepository.getOne(submissionId);

        boolean isAuthor = submission.getOwnerId() == userId;
        boolean isPublic = isPublicSubmission(submission.getAccessTag());
        boolean isSuperUser = user.isSuperuser();
        boolean hasTag = permissionsRepository.existsByAccessTagInAndAccessType(submission.getAccessTag(), accessType);

        switch (accessType) {
            case READ:
                return isPublic || isAuthor || hasTag || isSuperUser;
            case SUBMIT:
                return (isPublic && isAuthor) || isSuperUser || hasTag;
            case ATTACH:
                return hasTag || isSuperUser;
            case UPDATE:
            case DELETE:
                return isAuthor || isSuperUser || hasTag;
        }

        throw new IllegalStateException("Not supported access type ");
    }

    @Override
    public int getUsersCount() {
        return Math.toIntExact(userRepository.count());
    }

    private boolean isPublicSubmission(Set<AccessTag> accessTags) {
        return accessTags.stream().anyMatch(tag -> tag.getName().equals(PUBLIC_ACCESS_TAG));
    }
}
