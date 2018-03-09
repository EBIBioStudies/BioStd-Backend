package uk.ac.ebi.biostd.webapp.server.mng.security;

import uk.ac.ebi.biostd.authz.AuthorizationTemplate;
import uk.ac.ebi.biostd.authz.AuthzObject;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.authz.UserGroup;
import uk.ac.ebi.biostd.model.Submission;

public interface SecurityManager {

    void applyTemplate(AuthzObject gen, AuthorizationTemplate authorizationTemplate);

    boolean mayUserListAllSubmissions(User u);

    boolean mayUserReadSubmission(Submission sub, User user);

    boolean mayUserCreateSubmission(User usr);

    boolean mayUserUpdateSubmission(Submission oldSbm, User usr);

    boolean mayUserDeleteSubmission(Submission sbm, User usr);

    boolean mayUserAttachToSubmission(Submission s, User usr);

    boolean mayEveryoneReadSubmission(Submission submission);

    boolean mayUserCreateIdGenerator(User usr);

    User getUserById(long id);

    User getUserByLogin(String login);

    User getUserByEmail(String email);

    boolean mayUserManageTags(User user);

    boolean mayUserReadGroupFiles(User user, UserGroup g);

    boolean mayUserWriteGroupFiles(User user, UserGroup group);

    boolean mayUserControlExport(User usr);

    boolean mayUserLockExport(User usr);

    int getUsersNumber();

    User addInactiveUser(String email, String name);
}
