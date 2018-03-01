package uk.ac.ebi.biostd.webapp.application.security.service;

import java.util.List;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.Submission;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;
import uk.ac.ebi.biostd.webapp.application.security.entities.LoginRequest;
import uk.ac.ebi.biostd.webapp.application.security.entities.SignUpRequest;
import uk.ac.ebi.biostd.webapp.application.security.rest.model.UserData;

public interface ISecurityService {

    List<Submission> getAllowedProjects(long userId, AccessType accessType);

    User getPermissions(LoginRequest loginInformation);

    UserData signIn(String login, String password);

    void signOut(String securityKey);

    void addUser(SignUpRequest signUpRequest);

    void activate(String activationKey);

    User getUserByKey(String key);

    void resetPassword(String key, String password);

    void resetPasswordRequest(String email, String activationUrl);

    boolean hasPermission(long submissionId, long userId, AccessType accessType);

    int getUsersCount();
}
