package uk.ac.ebi.biostd.webapp.application.security.common;

import java.util.List;
import uk.ac.ebi.biostd.webapp.application.security.entities.LoginRequest;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.Submission;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;

public interface ISecurityService {

    List<Submission> getAllowedProjects(long userId, AccessType accessType);

    User getPermissions(LoginRequest loginInformation);

    String signIn(String login, String password);

    void signOut(String securityKey);

    void addUser(User user, String activationUrl);

    boolean activate(String activationKey);

    User getUserByKey(String key);

    boolean resetPassword(String key, String password);

    void resetPasswordRequest(String email, String activationUrl);
}
