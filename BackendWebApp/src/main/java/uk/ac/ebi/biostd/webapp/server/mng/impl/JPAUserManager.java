package uk.ac.ebi.biostd.webapp.server.mng.impl;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import lombok.AllArgsConstructor;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.authz.UserData;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.mng.SessionListener;
import uk.ac.ebi.biostd.webapp.server.mng.SessionManager;
import uk.ac.ebi.biostd.webapp.server.mng.UserManager;
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
    public int getUsersNumber() {
        return securityManager.getUsersNumber();
    }

    @Override
    public void sessionOpened(User u) {
    }

    @Override
    public void sessionClosed(User u) {
    }

    @Override
    public UserData getUserData(User user, String key) {
        EntityManager em = BackendConfig.getServiceManager().getEntityManager();

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
        EntityManager em = BackendConfig.getServiceManager().getEntityManager();

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

        EntityManager em = BackendConfig.getServiceManager().getEntityManager();

        Query q = em.createNamedQuery("UserData.getByTopic");

        q.setParameter("uid", user.getId());
        q.setParameter("topic", topic);

        List<UserData> res = q.getResultList();

        return res;
    }

    @Override
    public void storeUserData(UserData userData) {
        EntityManager em = BackendConfig.getServiceManager().getEntityManager();

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
}
