package uk.ac.ebi.biostd.webapp.server.mng;

import java.util.List;
import uk.ac.ebi.biostd.authz.Session;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.authz.UserData;
import uk.ac.ebi.biostd.authz.UserGroup;
import uk.ac.ebi.biostd.webapp.server.mng.AccountActivation.ActivationInfo;
import uk.ac.ebi.biostd.webapp.server.mng.exception.UserMngException;
import uk.ac.ebi.biostd.webapp.server.mng.security.SecurityException;

public interface UserManager {

    Session login(String login, String password, boolean passHash) throws SecurityException;

    User getUserByLogin(String uName);

    User getUserByEmail(String email);

    User getUserByLoginOrEmail(String loginOrEmail);

    User getUserBySSOSubject(String ssoSubject);

    int getUsersNumber();

    void addUser(User u, List<String[]> aux, boolean validateEmail, String actvURL) throws UserMngException;

    UserData getUserData(User user, String key);

    void storeUserData(UserData userData);

    boolean activateUser(ActivationInfo activationInfo) throws UserMngException;

    void passwordResetRequest(User user, String resetURL) throws UserMngException;

    void resetPassword(ActivationInfo activationInfo, String pass) throws UserMngException;

    List<UserData> getAllUserData(User user);

    List<UserData> getUserDataByTopic(User user, String topic);

    UserGroup getGroup(String groupName);

    void addGroup(UserGroup ug) throws UserMngException;

    void removeGroup(String grName) throws UserMngException;
}
