package uk.ac.ebi.biostd.webapp.server.mng.impl;

import com.pri.log.Log;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import uk.ac.ebi.biostd.authz.Session;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.authz.UserData;
import uk.ac.ebi.biostd.authz.UserGroup;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.mng.AccountActivation;
import uk.ac.ebi.biostd.webapp.server.mng.AccountActivation.ActivationInfo;
import uk.ac.ebi.biostd.webapp.server.mng.FileManager;
import uk.ac.ebi.biostd.webapp.server.mng.SessionListener;
import uk.ac.ebi.biostd.webapp.server.mng.SessionManager;
import uk.ac.ebi.biostd.webapp.server.mng.UserAuxXMLFormatter;
import uk.ac.ebi.biostd.webapp.server.mng.UserManager;
import uk.ac.ebi.biostd.webapp.server.mng.exception.InvalidKeyException;
import uk.ac.ebi.biostd.webapp.server.mng.exception.KeyExpiredException;
import uk.ac.ebi.biostd.webapp.server.mng.exception.ServiceException;
import uk.ac.ebi.biostd.webapp.server.mng.exception.SystemUserMngException;
import uk.ac.ebi.biostd.webapp.server.mng.exception.UserAlreadyActiveException;
import uk.ac.ebi.biostd.webapp.server.mng.exception.UserMngException;
import uk.ac.ebi.biostd.webapp.server.mng.exception.UserNotActiveException;
import uk.ac.ebi.biostd.webapp.server.mng.exception.UserNotFoundException;
import uk.ac.ebi.biostd.webapp.server.mng.security.SecurityException;
import uk.ac.ebi.biostd.webapp.server.mng.security.SecurityManager;

@AllArgsConstructor
public class JPAUserManager implements UserManager, SessionListener {

    private final SecurityManager securityManager;
    private final SessionManager sessionManager;

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
    public User getUserByLogin(String login) {
        return securityManager.getUserByLogin(login);
    }

    @Override
    public User getUserBySSOSubject(String ssoSubject) {
        return securityManager.getUserBySSOSubject(ssoSubject);
    }

    @Override
    public UserGroup getGroup(String groupName) {
        return securityManager.getGroup(groupName);
    }

    @Override
    public int getUsersNumber() {
        return securityManager.getUsersNumber();
    }

    @Override
    public synchronized void addUser(User user, List<String[]> aux, boolean validateEmail, String validateURL)
            throws UserMngException {

        user.setSecret(UUID.randomUUID().toString());
        user.setActive(true);

        if (aux != null) {
            user.setAuxProfileInfo(UserAuxXMLFormatter.buildXML(aux));
        }

        if (validateEmail) {
            UUID actKey = UUID.randomUUID();
            user.setActive(false);
            user.setActivationKey(actKey.toString());
            user.setKeyTime(System.currentTimeMillis());

            if (!AccountActivation.sendActivationRequest(user, actKey, validateURL)) {
                throw new SystemUserMngException("Email confirmation request can't be sent. Please try later");
            }
        }

        try {
            securityManager.addUser(user);
        } catch (ServiceException e) {
            throw new SystemUserMngException("System error", e);
        }

        if (!validateEmail) {
            try {
                createUserDropbox(user);
            } catch (IOException e) {
                Log.error("User directories/links were not created: " + e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void removeGroup(String gName) throws UserMngException {
        UserGroup ug = securityManager.getGroup(gName);

        if (ug == null) {
            throw new UserMngException("Group not found");
        }

        try {
            securityManager.removeGroup(ug.getId());
        } catch (ServiceException e) {
            throw new SystemUserMngException("System error", e);
        }

        if (!ug.isProject()) {
            return;
        }

        Path udpth = BackendConfig.getGroupDirPath(ug);
        Path llpth = BackendConfig.getGroupLinkPath(ug);

        FileManager fmgr = BackendConfig.getServiceManager().getFileManager();

        try {
            fmgr.deleteDirectory(udpth);

            try {
                if (llpth != null) {
                    Files.delete(llpth);
                }
            } catch (Exception e2) {
                Log.error("System can't delete symbolic links: " + e2.getMessage());
            }

        } catch (IOException e) {
            Log.error("Group directories were not removed: " + e.getMessage(), e);
            e.printStackTrace();
        }

    }

    @Override
    public synchronized void addGroup(UserGroup ug) throws UserMngException {
        ug.setSecret(UUID.randomUUID().toString());

        try {
            securityManager.addGroup(ug);
        } catch (ServiceException e) {
            throw new SystemUserMngException("System error", e);
        }

        if (!ug.isProject()) {
            return;
        }

        Path udpth = BackendConfig.getGroupDirPath(ug);
        Path llpth = BackendConfig.getGroupLinkPath(ug);

        try {
            Files.createDirectories(udpth);

            if (BackendConfig.isPublicDropboxes()) {
                try {
                    Files.setPosixFilePermissions(udpth.getParent(), BackendConfig.rwx__x__x);
                    Files.setPosixFilePermissions(udpth, BackendConfig.rwxrwxrwx);
                } catch (Exception e2) {
                    Log.error("Can't set directory permissions: " + e2.getMessage());
                }
            }

            if (llpth != null) {
                Files.createDirectories(llpth.getParent());
            }

            try {
                if (llpth != null) {
                    Files.createSymbolicLink(llpth, udpth);
                }
            } catch (Exception e2) {
                Log.error("System can't create symbolic links: " + e2.getMessage());
            }

        } catch (IOException e) {
            Log.error("Group directories were not created: " + e.getMessage(), e);
            e.printStackTrace();
        }

    }

    @Override
    public void sessionOpened(User u) {
    }

    @Override
    public void sessionClosed(User u) {
    }

    @Override
    public UserData getUserData(User user, String key) {
        EntityManager em = sessionManager.getSession().getEntityManager();

        Query q = em.createNamedQuery("UserData.get");

        q.setParameter("uid", user.getId());
        q.setParameter("key", key);

        List<UserData> res = q.getResultList();

        if (res.size() == 0) {
            return null;
        }

        return res.get(0);
    }

    @Override
    public List<UserData> getAllUserData(User user) {
        EntityManager em = sessionManager.getSession().getEntityManager();

        Query q = em.createNamedQuery("UserData.getAll");

        q.setParameter("uid", user.getId());

        List<UserData> res = q.getResultList();

        return res;
    }

    @Override
    public List<UserData> getUserDataByTopic(User user, String topic) {
        if (topic.length() == 0) {
            topic = null;
        }

        EntityManager em = sessionManager.getSession().getEntityManager();

        Query q = em.createNamedQuery("UserData.getByTopic");

        q.setParameter("uid", user.getId());
        q.setParameter("topic", topic);

        List<UserData> res = q.getResultList();

        return res;
    }

    @Override
    public void storeUserData(UserData userData) {
        EntityManager em = sessionManager.getSession().getEntityManager();

        EntityTransaction trn = em.getTransaction();

        trn.begin();

        if (userData.getData() == null) {
            Query q = em.createNamedQuery("UserData.get");

            q.setParameter("uid", userData.getUserId());
            q.setParameter("key", userData.getDataKey());

            List<UserData> res = q.getResultList();

            if (res.size() != 0) {
                em.remove(res.get(0));
            }
        } else {
            em.merge(userData);
        }

        trn.commit();
    }

    @Override
    public boolean activateUser(ActivationInfo activationInfo) throws UserMngException {
        EntityManager em = sessionManager.getSession().getEntityManager();

        EntityTransaction trn = em.getTransaction();

        User u = null;

        try {
            trn.begin();

            Query q = em.createNamedQuery(User.GetByEMailQuery);

            q.setParameter(User.EmailQueryParameter, activationInfo.email);

            List<User> res = q.getResultList();

            if (res.size() != 0) {
                u = res.get(0);
            }

            if (u == null) {
                throw new UserNotFoundException();
            }

            if (u.isActive()) {
                u = null;
                throw new UserAlreadyActiveException();
            }

            if (!activationInfo.key.equals(u.getActivationKey())) {
                u = null;
                throw new InvalidKeyException();
            }

            if ((System.currentTimeMillis() - u.getKeyTime()) > BackendConfig.getActivationTimeout()) {
                throw new KeyExpiredException();
            }

            u.setActive(true);
            u.setActivationKey(null);

        } catch (UserMngException e) {
            trn.rollback();

            throw e;
        } catch (Exception e) {
            trn.rollback();

            throw new SystemUserMngException("System error", e);
        } finally {
            if (trn.isActive() && !trn.getRollbackOnly()) {
                trn.commit();

                if (u != null) { //We also need to update a user cache
                    User cchUsr = securityManager.getUserById(u.getId());

                    if (cchUsr != null) {
                        cchUsr.setActive(true);
                        cchUsr.setActivationKey(null);
                    }
                }

                try {
                    createUserDropbox(u);
                } catch (IOException e) {
                    Log.error("User directories/links were not created: " + e.getMessage(), e);
                    e.printStackTrace();
                }

            }
        }

        return true;
    }

    private void createUserDropbox(User u) throws IOException {
        Path udpth = BackendConfig.getUserDirPath(u);
        Path llpth = BackendConfig.getUserLoginLinkPath(u);
        Path elpth = BackendConfig.getUserEmailLinkPath(u);

        Files.createDirectories(udpth);

        if (BackendConfig.isPublicDropboxes()) {
            try {
                Files.setPosixFilePermissions(udpth.getParent(), BackendConfig.rwx__x__x);
                Files.setPosixFilePermissions(udpth, BackendConfig.rwxrwxrwx);
            } catch (Exception e2) {
                Log.warn("Can't set directory permissions: " + e2.getMessage());
            }
        }

        if (llpth != null) {
            Files.createDirectories(llpth.getParent());
        }

        if (elpth != null) {
            Files.createDirectories(elpth.getParent());
        }

        try {
            if (llpth != null) {
                Files.createSymbolicLink(llpth, udpth);
            }

            if (elpth != null) {
                Files.createSymbolicLink(elpth, udpth);
            }
        } catch (Exception e2) {
            Log.warn("System can't create symbolic links: " + e2.getMessage());
        }

    }

    @Override
    public void resetPassword(ActivationInfo activationInfo, String pass) throws UserMngException {
        EntityManager em = sessionManager.getSession().getEntityManager();

        EntityTransaction trn = em.getTransaction();

        User u = null;

        try {
            trn.begin();

            Query q = em.createNamedQuery(User.GetByEMailQuery);

            q.setParameter(User.EmailQueryParameter, activationInfo.email);

            List<User> res = q.getResultList();

            if (res.size() != 0) {
                u = res.get(0);
            }

            if (u == null) {
                throw new UserNotFoundException();
            }

            if (!u.isActive()) {
                u = null;
                throw new UserNotActiveException();
            }

            String dbKey = u.getActivationKey();

            if (dbKey == null || dbKey.length() == 0 || !activationInfo.key.equals(dbKey)) {
                u = null;
                throw new InvalidKeyException();
            }

            if ((System.currentTimeMillis() - u.getKeyTime()) > BackendConfig.getActivationTimeout()) {
                throw new KeyExpiredException();
            }

            u.setPassword(pass);
            u.setActivationKey(null);

        } catch (UserMngException e) {
            trn.rollback();

            throw e;
        } catch (Exception e) {
            trn.rollback();

            throw new SystemUserMngException("System error", e);
        } finally {
            if (trn.isActive() && !trn.getRollbackOnly()) {
                trn.commit();

                if (u != null) { //We also need to update a user cache
                    User cchUsr = securityManager.getUserById(u.getId());

                    if (cchUsr != null) {
                        cchUsr.setPasswordDigest(u.getPasswordDigest());
                    }
                }
            }
        }

    }

    @Override
    public Session login(String login, String password, boolean passHash) throws SecurityException {
        User usr = securityManager.checkUserLogin(login, password, passHash);

        SessionManager sessMngr = sessionManager;

        Session sess = sessMngr.getSessionByUserId(usr.getId());

        if (sess == null) {
            sess = sessMngr.createSession(usr);
        }

        return sess;
    }

    @Override
    public Session loginUsingSSOToken(User user0, String ssoToken, String ssoSubject) throws SecurityException {
        User user = securityManager.checkUserSSOSubject(ssoSubject);

        SessionManager sessMngr = sessionManager;

        Session sess = sessMngr.getSessionByUserId(user.getId());

        if (sess == null) {
            sess = sessMngr.createSession(user);
        }

        sess.setSSOToken(ssoToken);
        return sess;
    }

    @Override
    public void linkSSOSubjectToUser(User user, String ssoSubject) throws UserMngException {
        EntityManager em = sessionManager.getSession().getEntityManager();
        EntityTransaction trn = em.getTransaction();
        User u = null;

        try {
            trn.begin();
            TypedQuery<User> q = em.createNamedQuery(User.GetByEMailQuery, User.class);
            q.setParameter(User.EmailQueryParameter, user.getEmail());

            List<User> res = q.getResultList();

            if (res.size() != 0) {
                u = res.get(0);
            }

            if (u == null) {
                throw new UserNotFoundException();
            }

            u.setSsoSubject(ssoSubject);

        } catch (UserMngException e) {
            trn.rollback();
            throw e;
        } catch (Exception e) {
            trn.rollback();
            throw new SystemUserMngException("System error", e);
        } finally {
            if (trn.isActive() && !trn.getRollbackOnly()) {
                trn.commit();
                if (u != null) {
                    // We also need to update a user cache
                    uk.ac.ebi.biostd.webapp.server.mng.security.SecurityManager secMan =
                            securityManager;
                    User cchUsr = secMan.getUserById(u.getId());
                    if (cchUsr != null) {
                        cchUsr.setSsoSubject(ssoSubject);
                    }
                    secMan.addUserSSOSubject(u, ssoSubject);
                }
            }
        }
    }

    @Override
    public void passwordResetRequest(User user, String resetURL) throws UserMngException {
        EntityManager em = sessionManager.getSession().getEntityManager();

        EntityTransaction trn = em.getTransaction();

        User u = null;

        try {
            trn.begin();

            if (user.getEmail() != null && user.getEmail().length() > 0) {
                Query q = em.createNamedQuery(User.GetByEMailQuery);

                q.setParameter(User.EmailQueryParameter, user.getEmail());

                List<User> res = q.getResultList();

                if (res.size() != 0) {
                    u = res.get(0);
                }
            } else {
                Query q = em.createNamedQuery(User.GetByLoginQuery);

                q.setParameter(User.LoginQueryParameter, user.getLogin());

                List<User> res = q.getResultList();

                if (res.size() != 0) {
                    u = res.get(0);
                }
            }

            if (u == null) {
                throw new UserNotFoundException();
            }

            if (!u.isActive()) {
                u = null;
                throw new UserNotActiveException();
            }

            UUID actKey = UUID.randomUUID();

            u.setActivationKey(actKey.toString());
            u.setKeyTime(System.currentTimeMillis());

            if (!AccountActivation.sendResetRequest(u, actKey, resetURL)) {
                throw new SystemUserMngException("Email with password reset details can't be sent. Please try later");
            }

        } catch (UserMngException e) {
            trn.rollback();

            throw e;
        } catch (Exception e) {
            trn.rollback();

            throw new SystemUserMngException("System error", e);
        } finally {
            if (trn.isActive() && !trn.getRollbackOnly()) {
                trn.commit();

                if (u != null) { //We also need to update a user cache
                    User cchUsr = securityManager.getUserById(u.getId());

                    if (cchUsr != null) {
                        cchUsr.setActivationKey(u.getActivationKey());
                        cchUsr.setKeyTime(u.getKeyTime());
                    }
                }
            }
        }
    }
}
