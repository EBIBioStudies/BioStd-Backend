package uk.ac.ebi.biostd.webapp.server;

import java.util.Collections;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import uk.ac.ebi.biostd.authz.AccessTag;
import uk.ac.ebi.biostd.authz.BuiltInGroups;
import uk.ac.ebi.biostd.authz.BuiltInUsers;
import uk.ac.ebi.biostd.authz.SystemAction;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.authz.UserGroup;
import uk.ac.ebi.biostd.authz.acr.DelegatePermGrpACR;
import uk.ac.ebi.biostd.authz.acr.SystemPermGrpACR;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;

public class DBInitializer {

    public static void init() {
        EntityManager em = BackendConfig.getServiceManager().getSessionManager().getEntityManager();

        EntityTransaction trn = em.getTransaction();

        trn.begin();

        User sysUser = null;

        for (BuiltInUsers usr : BuiltInUsers.values()) {
            User u = new User();

            u.setLogin(usr.getUserName());
            u.setFullName(usr.getDescription());
            u.setSuperuser(false);

            if (usr == BuiltInUsers.System) {
                sysUser = u;
            }

            em.persist(u);
        }

        UserGroup everyone = null;
        UserGroup authd = null;

        for (BuiltInGroups grp : BuiltInGroups.values()) {
            UserGroup g = new UserGroup();

            g.setName(grp.getGroupName());
            g.setDescription(grp.getDescription());
            g.setProject(false);
            g.setOwner(sysUser);

            if (grp == BuiltInGroups.AuthenticatedGroup) {
                authd = g;
            } else if (grp == BuiltInGroups.EveryoneGroup) {
                everyone = g;
            }

            em.persist(g);
        }

        DelegatePermGrpACR anyRead = new DelegatePermGrpACR();
        anyRead.setSubject(everyone);
        anyRead.setAction(SystemAction.READ);
        anyRead.setAllow(true);

        AccessTag tag = new AccessTag();

        tag.setName(BackendConfig.PublicTag);
        tag.setOwner(sysUser);
        tag.setDelegatePermissionForGroupACRs(Collections.singleton(anyRead));

        anyRead.setHost(tag);

        em.persist(anyRead);
        em.persist(tag);

        SystemPermGrpACR acr = new SystemPermGrpACR();
        acr.setAction(SystemAction.CREATESUBM);
        acr.setAllow(true);
        acr.setSubject(authd);

        em.persist(acr);

        trn.commit();

    }
}
