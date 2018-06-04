package uk.ac.ebi.biostd.webapp.server.mng;

import uk.ac.ebi.biostd.authz.User;

public interface UserManager {

    void addPermision(long userId, String domain);

    User getUserByLogin(String uName);

    User getUserByEmail(String email);

    User getUserByLoginOrEmail(String loginOrEmail);

    int getUsersNumber();

    User addInactiveUser(String email, String name);
}
