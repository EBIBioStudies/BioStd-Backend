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

package uk.ac.ebi.biostd.idgen;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import uk.ac.ebi.biostd.authz.ACR.Permit;
import uk.ac.ebi.biostd.authz.AuthzObject;
import uk.ac.ebi.biostd.authz.PermissionProfile;
import uk.ac.ebi.biostd.authz.SystemAction;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.authz.UserGroup;
import uk.ac.ebi.biostd.idgen.acr.DomainPermGrpACR;
import uk.ac.ebi.biostd.idgen.acr.DomainPermUsrACR;
import uk.ac.ebi.biostd.idgen.acr.DomainProfGrpACR;
import uk.ac.ebi.biostd.idgen.acr.DomainProfUsrACR;

@Entity
public class Domain implements AuthzObject {

    private long id;
    private Collection<DomainProfGrpACR> profileForGroupACRs;
    private Collection<DomainProfUsrACR> profileForUserACRs;
    private Collection<DomainPermUsrACR> permissionForUserACRs;
    private Collection<DomainPermGrpACR> permissionForGroupACRs;
    private Collection<IdGen> templates;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    public Collection<DomainProfGrpACR> getProfileForGroupACRs() {
        return profileForGroupACRs;
    }

    public void setProfileForGroupACRs(Collection<DomainProfGrpACR> profileForGroupACRs) {
        this.profileForGroupACRs = profileForGroupACRs;
    }

    @Override
    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    public Collection<DomainProfUsrACR> getProfileForUserACRs() {
        return profileForUserACRs;
    }

    public void setProfileForUserACRs(Collection<DomainProfUsrACR> profileForUserACRs) {
        this.profileForUserACRs = profileForUserACRs;
    }

    @Override
    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    public Collection<DomainPermUsrACR> getPermissionForUserACRs() {
        return permissionForUserACRs;
    }

    public void setPermissionForUserACRs(Collection<DomainPermUsrACR> permissionForUserACRs) {
        this.permissionForUserACRs = permissionForUserACRs;
    }

    @Override
    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    public Collection<DomainPermGrpACR> getPermissionForGroupACRs() {
        return permissionForGroupACRs;
    }

    public void setPermissionForGroupACRs(Collection<DomainPermGrpACR> permissionForGroupACRs) {
        this.permissionForGroupACRs = permissionForGroupACRs;
    }

    @Override
    public void addPermissionForUserACR(User u, SystemAction act, boolean allow) {
        DomainPermUsrACR acr = new DomainPermUsrACR();

        acr.setSubject(u);
        acr.setHost(this);
        acr.setAction(act);
        acr.setAllow(allow);

        if (permissionForUserACRs == null) {
            permissionForUserACRs = new ArrayList<>();
        }

        permissionForUserACRs.add(acr);
    }

    @Override
    public void addPermissionForGroupACR(UserGroup ug, SystemAction act, boolean allow) {
        DomainPermGrpACR acr = new DomainPermGrpACR();

        acr.setSubject(ug);
        acr.setHost(this);
        acr.setAction(act);
        acr.setAllow(allow);

        if (permissionForGroupACRs == null) {
            permissionForGroupACRs = new ArrayList<>();
        }

        permissionForGroupACRs.add(acr);
    }

    @Override
    public void addProfileForUserACR(User u, PermissionProfile pp) {
        DomainProfUsrACR acr = new DomainProfUsrACR();

        acr.setSubject(u);
        acr.setHost(this);
        acr.setProfile(pp);

        if (profileForUserACRs == null) {
            profileForUserACRs = new ArrayList<>();
        }

        profileForUserACRs.add(acr);
    }

    @Override
    public void addProfileForGroupACR(UserGroup ug, PermissionProfile pp) {
        DomainProfGrpACR acr = new DomainProfGrpACR();

        acr.setSubject(ug);
        acr.setHost(this);
        acr.setProfile(pp);

        if (profileForGroupACRs == null) {
            profileForGroupACRs = new ArrayList<>();
        }

        profileForGroupACRs.add(acr);
    }

    @Override
    public Permit checkPermission(SystemAction act, User user) {
        return Permit.checkPermission(act, user, this);
    }


}
