package uk.ac.ebi.biostd.webapp.application.security.service;

import java.util.Optional;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;
import uk.ac.ebi.biostd.webapp.application.security.entities.LoginRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.ResetPasswordRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.RetryActivationRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignUpRequest;
import uk.ac.ebi.biostd.webapp.application.security.rest.model.UserData;

public interface ISecurityService {

    User getUser(LoginRequest loginInformation);

    User getUser(long userId);

    UserData signIn(String login, String password);

    void signOut(String securityKey);

    void addUser(SignUpRequest signUpRequest);

    void activate(String activationKey);

    Optional<User> getUserByKey(String key);

    void resetPassword(String key, String password);

    void resetPasswordRequest(ResetPasswordRequest request);

    boolean hasPermission(long submissionId, long userId, AccessType accessType);

    int getUsersCount();

    User addInactiveUserIfNotExist(String email, String name);

    void addPermission(long id, String domain);

    void retryActivation(RetryActivationRequest activationRequest);
}
