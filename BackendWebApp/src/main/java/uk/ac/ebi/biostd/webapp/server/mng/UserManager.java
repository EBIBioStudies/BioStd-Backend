package uk.ac.ebi.biostd.webapp.server.mng;

import java.util.List;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.authz.UserData;

public interface UserManager {

    User getUserByLogin(String uName);

    User getUserByEmail(String email);

    User getUserByLoginOrEmail(String loginOrEmail);

    int getUsersNumber();

    UserData getUserData(User user, String key);

    void storeUserData(UserData userData);

    List<UserData> getAllUserData(User user);

    List<UserData> getUserDataByTopic(User user, String topic);
}
