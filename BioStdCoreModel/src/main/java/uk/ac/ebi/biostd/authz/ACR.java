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


public interface ACR {

    Permit checkPermission(SystemAction act, User user);

    AuthzSubject getSubject();

    PermissionUnit getPermissionUnit();

    public enum Permit {
        ALLOW, DENY, UNDEFINED;

        static public Permit checkPermission(SystemAction act, User user, AuthzObject acl) {
            boolean allow = false;

            if (acl.getProfileForGroupACRs() != null) {
                for (ACR b : acl.getProfileForGroupACRs()) {
                    Permit p = b.checkPermission(act, user);
                    if (p == Permit.DENY) {
                        return Permit.DENY;
                    } else if (p == Permit.ALLOW) {
                        allow = true;
                    }
                }
            }

            if (acl.getProfileForUserACRs() != null) {
                for (ACR b : acl.getProfileForUserACRs()) {
                    Permit p = b.checkPermission(act, user);
                    if (p == Permit.DENY) {
                        return Permit.DENY;
                    } else if (p == Permit.ALLOW) {
                        allow = true;
                    }
                }
            }

            if (acl.getPermissionForUserACRs() != null) {
                for (ACR b : acl.getPermissionForUserACRs()) {
                    Permit p = b.checkPermission(act, user);
                    if (p == Permit.DENY) {
                        return Permit.DENY;
                    } else if (p == Permit.ALLOW) {
                        allow = true;
                    }
                }
            }

            if (acl.getPermissionForGroupACRs() != null) {
                for (ACR b : acl.getPermissionForGroupACRs()) {
                    Permit p = b.checkPermission(act, user);
                    if (p == Permit.DENY) {
                        return Permit.DENY;
                    } else if (p == Permit.ALLOW) {
                        allow = true;
                    }
                }
            }

            return allow ? Permit.ALLOW : Permit.UNDEFINED;
        }
    }
}
