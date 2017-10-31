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

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
public class GenPermACR<SubjT extends AuthzSubject> extends Permission implements PermissionACR {

    private long id;
    private SubjT subject;
    private SystemAction action;
    private boolean allow;

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public Permit checkPermission(SystemAction act, User user) {
        if (!subject.isUserCompatible(user)) {
            return Permit.UNDEFINED;
        }

        return checkPermission(act);
    }

    @Override
    public Permit checkPermission(SystemAction act) {
        if (act != action) {
            return Permit.UNDEFINED;
        }

        return allow ? Permit.ALLOW : Permit.DENY;
    }

    @Override
    @ManyToOne
    @JoinColumn(name = "subject_id")
    public SubjT getSubject() {
        return subject;
    }

    public void setSubject(SubjT gb) {
        subject = gb;
    }

    @Override
    @Enumerated(EnumType.STRING)
    public SystemAction getAction() {
        return action;
    }

    @Override
    public void setAction(SystemAction action) {
        this.action = action;
    }

    @Override
    public boolean isAllow() {
        return allow;
    }

    @Override
    public void setAllow(boolean allow) {
        this.allow = allow;
    }


    @Override
    @Transient
    public Permission getPermissionUnit() {
        return this;
    }


}
