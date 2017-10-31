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

package uk.ac.ebi.biostd.authz;

import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import uk.ac.ebi.biostd.authz.ACR.Permit;

@Entity
public class PermissionProfile implements PermissionUnit {

    private long id;
    private String name;
    private String description;
    private Collection<Permission> permissions;
    private Collection<PermissionProfile> profiles;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public Collection<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<Permission> permissions) {
        this.permissions = permissions;
    }

    @ManyToMany
    public Collection<PermissionProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Collection<PermissionProfile> profiles) {
        this.profiles = profiles;
    }

    public boolean isPartOf(PermissionProfile pb) {
        if (pb.getProfiles() == null) {
            return false;
        }

        for (PermissionProfile gb : pb.getProfiles()) {
            if (equals(gb)) {
                return true;
            }

            if (isPartOf(gb)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Permit checkPermission(SystemAction act) {
        boolean allw = false;

        if (permissions != null) {
            for (Permission p : permissions) {
                if (act == p.getAction()) {
                    if (p.isAllow()) {
                        allw = true;
                    } else {
                        return Permit.DENY;
                    }
                }
            }
        }

        if (profiles != null) {
            for (PermissionProfile pp : profiles) {
                Permit r = pp.checkPermission(act);

                if (r == Permit.DENY) {
                    return Permit.DENY;
                } else if (r == Permit.ALLOW) {
                    allw = true;
                }
            }
        }

        return allw ? Permit.ALLOW : Permit.UNDEFINED;

    }


}
