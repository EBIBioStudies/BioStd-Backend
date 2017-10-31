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
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import uk.ac.ebi.biostd.authz.ACR.Permit;
import uk.ac.ebi.biostd.authz.AuthzObject;
import uk.ac.ebi.biostd.authz.PermissionProfile;
import uk.ac.ebi.biostd.authz.SystemAction;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.authz.UserGroup;
import uk.ac.ebi.biostd.idgen.acr.CounterPermGrpACR;
import uk.ac.ebi.biostd.idgen.acr.CounterPermUsrACR;
import uk.ac.ebi.biostd.idgen.acr.CounterProfGrpACR;
import uk.ac.ebi.biostd.idgen.acr.CounterProfUsrACR;

@Entity
@NamedQueries({@NamedQuery(name = Counter.GetByNameQuery, query = "SELECT t FROM Counter t where t.name=:name")})
@Table(indexes = {@Index(name = "name_idx", columnList = "name", unique = true)})
public class Counter implements AuthzObject {

    public static final String GetByNameQuery = "Counter.getByName";
    private long id;
    private String name;
    private long maxCount;
    private Collection<CounterProfGrpACR> profileForGroupACRs;
    private Collection<CounterProfUsrACR> profileForUserACRs;
    private Collection<CounterPermUsrACR> permissionForUserACRs;
    private Collection<CounterPermGrpACR> permissionForGroupACRs;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(long maxCount) {
        this.maxCount = maxCount;
    }

    @Transient
    public long getNextNumber() {
        return ++maxCount;
    }

    @Transient
    public long incrementByNum(int num) {
        long first = maxCount + 1;

        maxCount += num;

        return first;
    }

    @Override
    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    public Collection<CounterProfGrpACR> getProfileForGroupACRs() {
        return profileForGroupACRs;
    }

    public void setProfileForGroupACRs(Collection<CounterProfGrpACR> profileForGroupACRs) {
        this.profileForGroupACRs = profileForGroupACRs;
    }

    @Override
    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    public Collection<CounterProfUsrACR> getProfileForUserACRs() {
        return profileForUserACRs;
    }

    public void setProfileForUserACRs(Collection<CounterProfUsrACR> profileForUserACRs) {
        this.profileForUserACRs = profileForUserACRs;
    }

    @Override
    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    public Collection<CounterPermUsrACR> getPermissionForUserACRs() {
        return permissionForUserACRs;
    }

    public void setPermissionForUserACRs(Collection<CounterPermUsrACR> permissionForUserACRs) {
        this.permissionForUserACRs = permissionForUserACRs;
    }

    @Override
    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    public Collection<CounterPermGrpACR> getPermissionForGroupACRs() {
        return permissionForGroupACRs;
    }

    public void setPermissionForGroupACRs(Collection<CounterPermGrpACR> permissionForGroupACRs) {
        this.permissionForGroupACRs = permissionForGroupACRs;
    }

    @Override
    public Permit checkPermission(SystemAction act, User user) {
        return Permit.checkPermission(act, user, this);
    }

    @Override
    public void addPermissionForUserACR(User u, SystemAction act, boolean allow) {
        CounterPermUsrACR acr = new CounterPermUsrACR();

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
        CounterPermGrpACR acr = new CounterPermGrpACR();

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
        CounterProfUsrACR acr = new CounterProfUsrACR();

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
        CounterProfGrpACR acr = new CounterProfGrpACR();

        acr.setSubject(ug);
        acr.setHost(this);
        acr.setProfile(pp);

        if (profileForGroupACRs == null) {
            profileForGroupACRs = new ArrayList<>();
        }

        profileForGroupACRs.add(acr);
    }

}
