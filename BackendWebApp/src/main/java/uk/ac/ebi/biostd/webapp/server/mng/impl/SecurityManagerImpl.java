package uk.ac.ebi.biostd.webapp.server.mng.impl;

import static uk.ac.ebi.biostd.webapp.application.legacy.common.JpaResultHelper.getSingleResult;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.biostd.authz.AuthorizationTemplate;
import uk.ac.ebi.biostd.authz.AuthzObject;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.authz.UserGroup;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType;
import uk.ac.ebi.biostd.webapp.application.security.service.ISecurityService;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.mng.security.SecurityManager;

@Slf4j
@AllArgsConstructor
public class SecurityManagerImpl implements SecurityManager {

    private final LoadingCache<Long, User> usersMap = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<Long, User>() {
                @Override
                public User load(Long key) {
                    return getUserByIdForCache(key);
                }
            });

    private final ISecurityService securityService;

    @Override
    public int getUsersNumber() {
        return securityService.getUsersCount();
    }

    @Override
    public User addInactiveUser(String email, String name) {
        User user = getUserById(securityService.addInactiveUserIfNotExist(email, name).getId());
        return user;
    }

    @Override
    public void addPermission(long userId, String domain) {
        securityService.addPermission(userId, domain);
    }

    @Override
    public boolean mayUserCreateSubmission(User user) {
        return true;
    }

    @Override
    public boolean mayUserUpdateSubmission(Submission sbm, User user) {
        return securityService.hasPermission(sbm.getId(), user.getId(), AccessType.UPDATE);
    }

    @Override
    public boolean mayUserDeleteSubmission(Submission sbm, User user) {
        return securityService.hasPermission(sbm.getId(), user.getId(), AccessType.DELETE);
    }

    @Override
    public boolean mayUserReadSubmission(Submission sbm, User user) {
        return securityService.hasPermission(sbm.getId(), user.getId(), AccessType.READ);
    }

    @Override
    public boolean mayUserAttachToSubmission(Submission sbm, User user) {
        return securityService.hasPermission(sbm.getId(), user.getId(), AccessType.ATTACH);
    }

    @Override
    public boolean mayUserListAllSubmissions(User user) {
        return false;
    }

    @Override
    public boolean mayEveryoneReadSubmission(Submission submission) {
        return false;
    }

    @Override
    public boolean mayUserCreateIdGenerator(User user) {
        return false;
    }

    @Override
    public void applyTemplate(AuthzObject obj, AuthorizationTemplate tpl) {
    }

    @Override
    @SneakyThrows
    public User getUserById(long id) {
        return usersMap.get(id);
    }

    private User getUserByIdForCache(long id) {
        EntityManager entityManager = BackendConfig.getServiceManager().getEntityManager();
        TypedQuery<User> typedQuery = entityManager
                .createNamedQuery(User.GetByIdQuery, User.class)
                .setParameter("id", id);
        return getSingleResult(typedQuery);
    }

    @Override
    public User getUserByLogin(String login) {
        EntityManager entityManager = BackendConfig.getServiceManager().getEntityManager();
        TypedQuery<User> typedQuery = entityManager
                .createNamedQuery(User.GetByLoginQuery, User.class)
                .setParameter("login", login);
        return getSingleResult(typedQuery);
    }

    @Override
    public User getUserByEmail(String email) {
        EntityManager em = BackendConfig.getServiceManager().getEntityManager();
        TypedQuery<User> typedQuery = em.createNamedQuery(User.GetByEMailQuery, User.class);
        typedQuery.setParameter("email", email);
        return getSingleResult(typedQuery);
    }


    @Override
    public boolean mayUserManageTags(User user) {
        return false;
    }

    @Override
    public boolean mayUserControlExport(User user) {
        return false;
    }

    @Override
    public boolean mayUserLockExport(User user) {
        return false;
    }

    @Override
    public boolean mayUserReadGroupFiles(User user, UserGroup g) {
        if (user.getGroups() == null) {
            return false;
        }

        for (UserGroup ug : user.getGroups()) {
            if (ug.getId() == g.getId()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mayUserWriteGroupFiles(User user, UserGroup g) {
        return mayUserReadGroupFiles(user, g);
    }
}
