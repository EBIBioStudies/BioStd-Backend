/**
 * Copyright 2014-2017 Functional Genomics Development Team, European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * @author Mikhail Gostev <gostev@gmail.com>
 **/

package uk.ac.ebi.biostd.webapp.server.mng.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.biostd.authz.ACR;
import uk.ac.ebi.biostd.authz.ACR.Permit;
import uk.ac.ebi.biostd.authz.AccessTag;
import uk.ac.ebi.biostd.authz.AuthorizationTemplate;
import uk.ac.ebi.biostd.authz.AuthzObject;
import uk.ac.ebi.biostd.authz.Permission;
import uk.ac.ebi.biostd.authz.PermissionProfile;
import uk.ac.ebi.biostd.authz.SystemAction;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.authz.UserGroup;
import uk.ac.ebi.biostd.authz.acr.GroupPermGrpACR;
import uk.ac.ebi.biostd.authz.acr.GroupPermUsrACR;
import uk.ac.ebi.biostd.authz.acr.GroupProfGrpACR;
import uk.ac.ebi.biostd.authz.acr.GroupProfUsrACR;
import uk.ac.ebi.biostd.authz.acr.TemplatePermGrpACR;
import uk.ac.ebi.biostd.authz.acr.TemplatePermUsrACR;
import uk.ac.ebi.biostd.authz.acr.TemplateProfGrpACR;
import uk.ac.ebi.biostd.authz.acr.TemplateProfUsrACR;
import uk.ac.ebi.biostd.model.SecurityObject;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.mng.security.SecurityManager;

public class SecurityManagerImpl implements SecurityManager {

    private static Logger log;

    private User anonUser;
    private Collection<ACR> systemACR;
    private final Map<Long, UserGroup> groupMap = new HashMap<>();
    private final Map<Long, User> userMap = new HashMap<>();
    private final Map<String, User> userEmailMap = new HashMap<>();
    private final Map<String, User> userLoginMap = new HashMap<>();
    private final Map<String, User> userSSOSubjectMap = new HashMap<>();
    private final Map<String, UserGroup> groupNameMap = new HashMap<>();
    private final Map<Long, PermissionProfile> profileMap = new HashMap<>();


    public SecurityManagerImpl() {
        if (log == null) {
            log = LoggerFactory.getLogger(getClass());
        }
    }

    @Override
    public int getUsersNumber() {
        return userMap.size();
    }

    private User detachUser(User u) // to pull user structure from the DB. We need this to overcome lazy loading
    {
        User du = userMap.get(u.getId());

        if (du != null) {
            return du;
        }

        du = User.makeCopy(u);

        userMap.put(du.getId(), du);

        if (du.getEmail() != null && du.getEmail().length() > 0) {
            userEmailMap.put(du.getEmail(), du);
        }

        if (du.getLogin() != null && du.getLogin().length() > 0) {
            userLoginMap.put(du.getLogin(), du);
        }

        if (du.getSsoSubject() != null && du.getSsoSubject().length() > 0) {
            userSSOSubjectMap.put(du.getSsoSubject(), du);
        }

        if (u.getGroups() != null) {
            Set<UserGroup> grps = new HashSet<>();

            for (UserGroup g : u.getGroups()) {
                grps.add(detachGroup(g, true));
            }

            du.setGroups(grps);
        }

        return du;
    }

    private PermissionProfile detachProfile(
            PermissionProfile pr) // to pull profile structure from the DB. We need this to overcome lazy loading
    {
        PermissionProfile np = profileMap.get(pr.getId());

        if (np != null) {
            return np;
        }

        np = new PermissionProfile();

        np.setId(pr.getId());
        np.setDescription(pr.getDescription());

        profileMap.put(np.getId(), np);

        if (pr.getPermissions() != null && pr.getPermissions().size() > 0) {
            Collection<Permission> pms = new ArrayList<>(pr.getPermissions().size());

            for (Permission pm : pr.getPermissions()) {
                Permission npm = new Permission();
                npm.setAction(pm.getAction());
                npm.setAllow(pm.isAllow());
                npm.setId(pm.getId());

                pms.add(npm);
            }

            np.setPermissions(pms);
        }

        if (pr.getProfiles() != null && pr.getProfiles().size() > 0) {
            Collection<PermissionProfile> pps = new ArrayList<>(pr.getProfiles().size());

            for (PermissionProfile pp : pr.getProfiles()) {
                pps.add(detachProfile(pp));
            }

            np.setProfiles(pps);
        }

        return np;
    }

    private UserGroup detachGroup(UserGroup g,
            boolean ldUsr) // to pull group structure from the DB. We need this to overcome lazy loading
    {
        UserGroup ug = groupMap.get(g.getId());

        if (ug != null) {
            return ug;
        }

        ug = new UserGroup();

        ug.setDescription(g.getDescription());
        ug.setId(g.getId());
        ug.setName(g.getName());
        ug.setOwner(detachUser(g.getOwner()));
        ug.setProject(g.isProject());
        ug.setSecret(g.getSecret());

        groupMap.put(ug.getId(), ug);
        groupNameMap.put(ug.getName(), ug);

        if (g.getUsers() != null && g.getUsers().size() > 0 && ldUsr) {
            Set<User> usrs = new HashSet<>();

            for (User u : g.getUsers()) {
                usrs.add(detachUser(u));
            }

            ug.setUsers(usrs);
        }

        if (g.getGroups() != null && g.getGroups().size() > 0) {
            Collection<UserGroup> grps = new ArrayList<>(g.getGroups().size());

            for (UserGroup sg : g.getGroups()) {
                grps.add(detachGroup(sg, ldUsr));
            }

            ug.setGroups(grps);
        }

        if (g.getPermissionForUserACRs() != null) {
            for (GroupPermUsrACR acr : g.getPermissionForUserACRs()) {
                ug.addPermissionForUserACR(detachUser(acr.getSubject()), acr.getAction(), acr.isAllow());
            }
        }

        if (g.getPermissionForGroupACRs() != null) {
            for (GroupPermGrpACR acr : g.getPermissionForGroupACRs()) {
                ug.addPermissionForGroupACR(
                        acr.getSubject().getId() == ug.getId() ? ug : detachGroup(acr.getSubject(), true),
                        acr.getAction(), acr.isAllow());
            }
        }

        if (g.getProfileForUserACRs() != null) {
            for (GroupProfUsrACR acr : g.getProfileForUserACRs()) {
                ug.addProfileForUserACR(detachUser(acr.getSubject()), detachProfile(acr.getProfile()));
            }
        }

        if (g.getProfileForGroupACRs() != null) {
            for (GroupProfGrpACR acr : g.getProfileForGroupACRs()) {
                ug.addProfileForGroupACR(
                        acr.getSubject().getId() == ug.getId() ? ug : detachGroup(acr.getSubject(), true),
                        detachProfile(acr.getProfile()));
            }
        }

        return ug;
    }


    private boolean checkSystemPermission(SystemAction act, User usr) {
        if (usr.isSuperuser()) {
            return true;
        }

        boolean allow = false;

        for (ACR acr : systemACR) {
            Permit p = acr.checkPermission(act, usr);

            if (p == Permit.DENY) {
                return false;
            } else if (p == Permit.ALLOW) {
                allow = true;
            }
        }

        return allow;
    }

    /**
     * TODO: add read and only create public permission.
     */
    @Override
    public boolean mayUserCreateSubmission(User usr) {
        return true;
    }

    @Override
    public boolean mayUserUpdateSubmission(Submission sbm, User usr) {
        return checkSubmissionPermission(sbm, usr, SystemAction.CHANGE);
    }

    @Override
    public boolean mayUserDeleteSubmission(Submission sbm, User usr) {
        return checkSubmissionPermission(sbm, usr, SystemAction.DELETE);
    }

    @Override
    public boolean mayUserReadSubmission(Submission sbm, User usr) {
        return checkSubmissionPermission(sbm, usr, SystemAction.READ);
    }

    @Override
    public boolean mayUserAttachToSubmission(Submission sbm, User usr) {
        return checkSubmissionPermission(sbm, usr, SystemAction.ATTACHSUBM);
    }

    @Override
    public boolean mayUserListAllSubmissions(User usr) {
        return checkSystemPermission(SystemAction.LISTALLSUBM, usr);
    }

    private boolean checkSubmissionPermission(Submission sbm, User usr, SystemAction act) {
        if (sbm.getOwner().equals(usr) || usr.isSuperuser()) {
            return true;
        }

        return checkObjectPermission(sbm, usr, act);
    }


    @Override
    public boolean mayEveryoneReadSubmission(Submission submission) {
        if (anonUser.getLogin().equals(submission.getOwner().getLogin())) {
            return true;
        }

        return checkObjectPermission(submission, anonUser, SystemAction.READ);
    }

    private boolean checkObjectPermission(SecurityObject obj, User usr, SystemAction act) {
        boolean allow = false;

        if (obj.getAccessTags() == null) {
            return false;
        }

        for (AccessTag atg : obj.getAccessTags()) {
            Permit p = atg.checkDelegatePermission(act, usr);

            if (p == Permit.DENY) {
                return false;
            } else if (p == Permit.ALLOW) {
                allow = true;
            }
        }

        return allow;
    }

    @Override
    public boolean mayUserCreateIdGenerator(User usr) {
        return checkSystemPermission(SystemAction.CREATEIDGEN, usr);
    }

    @Override
    public void applyTemplate(AuthzObject obj, AuthorizationTemplate tpl) {
        Collection<TemplatePermUsrACR> p4u = tpl.getPermissionForUserACRs();

        if (p4u != null) {
            for (TemplatePermUsrACR acr : p4u) {
                obj.addPermissionForUserACR(acr.getSubject(), acr.getAction(), acr.isAllow());
            }
        }

        Collection<TemplatePermGrpACR> p4g = tpl.getPermissionForGroupACRs();

        if (p4g != null) {
            for (TemplatePermGrpACR acr : p4g) {
                obj.addPermissionForGroupACR(acr.getSubject(), acr.getAction(), acr.isAllow());
            }
        }

        Collection<TemplateProfUsrACR> r4u = tpl.getProfileForUserACRs();

        if (r4u != null) {
            for (TemplateProfUsrACR acr : r4u) {
                obj.addProfileForUserACR(acr.getSubject(), acr.getProfile());
            }
        }

        Collection<TemplateProfGrpACR> r4g = tpl.getProfileForGroupACRs();

        if (r4u != null) {
            for (TemplateProfGrpACR acr : r4g) {
                obj.addProfileForGroupACR(acr.getSubject(), acr.getProfile());
            }
        }

    }

    @Override
    public User getAnonymousUser() {
        return anonUser;
    }

    @Override
    public User getUserById(long id) {
        EntityManager em = BackendConfig.getEntityManagerFactory().createEntityManager();
        TypedQuery<User> uq = em.createNamedQuery(User.GetByIdQuery, User.class);
        uq.setParameter("id", id);
        return uq.getSingleResult();
    }

    @Override
    public User getUserByLogin(String login) {
        return userLoginMap.get(login);
    }

    @Override
    public User getUserByEmail(String email) {
        return userEmailMap.get(email);
    }

    @Override
    public boolean mayUserManageTags(User usr) {
        return checkSystemPermission(SystemAction.MANAGETAGS, usr);
    }

    @Override
    public boolean mayUserControlExport(User usr) {
        return checkSystemPermission(SystemAction.CONTROLEXPORT, usr);
    }

    @Override
    public boolean mayUserLockExport(User usr) {
        return checkSystemPermission(SystemAction.LOCKEXPORT, usr);
    }


    @Override
    public UserGroup getGroup(String name) {
        return groupNameMap.get(name);
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
