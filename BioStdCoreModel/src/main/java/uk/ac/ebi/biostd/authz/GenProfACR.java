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

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@MappedSuperclass
public class GenProfACR<SubjT extends AuthzSubject> implements ProfileACR, PermissionUnit {

    private long id;
    private PermissionProfile profile;
    private SubjT subject;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public Permit checkPermission(SystemAction act, User user) {
        if (!subject.isUserCompatible(user)) {
            return Permit.UNDEFINED;
        }

        return profile.checkPermission(act);
    }

    @Override
    public Permit checkPermission(SystemAction act) {
        return profile.checkPermission(act);
    }

    @OneToOne
    public PermissionProfile getProfile() {
        return profile;
    }

    public void setProfile(PermissionProfile profile) {
        this.profile = profile;
    }

    @Override
    @ManyToOne
    public SubjT getSubject() {
        return subject;
    }

    public void setSubject(SubjT gb) {
        subject = gb;
    }

    @Override
    @Transient
    public PermissionProfile getPermissionUnit() {
        return profile;
    }


}
