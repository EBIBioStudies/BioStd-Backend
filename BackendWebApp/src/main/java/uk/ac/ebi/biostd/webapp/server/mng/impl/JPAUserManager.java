package uk.ac.ebi.biostd.webapp.server.mng.impl;

import lombok.AllArgsConstructor;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.server.mng.UserManager;
import uk.ac.ebi.biostd.webapp.server.mng.security.SecurityManager;

@AllArgsConstructor
public class JPAUserManager implements UserManager {

    private final SecurityManager securityManager;

    @Override
    public User getUserByEmail(String email) {
        return securityManager.getUserByEmail(email);
    }

    @Override
    public User getUserByLoginOrEmail(String loginOrEmail) {
        User user = getUserByLogin(loginOrEmail);
        return user == null ? getUserByEmail(loginOrEmail) : user;
    }

    @Override
    public User addInactiveUser(String email, String name) {
        return securityManager.addInactiveUser(email, name);
    }

    @Override
    public User getUserByLogin(String login) {
        return securityManager.getUserByLogin(login);
    }

    @Override
    public int getUsersNumber() {
        return securityManager.getUsersNumber();
    }
}
