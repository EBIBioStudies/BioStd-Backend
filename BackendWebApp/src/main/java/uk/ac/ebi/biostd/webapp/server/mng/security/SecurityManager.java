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

package uk.ac.ebi.biostd.webapp.server.mng.security;

import uk.ac.ebi.biostd.authz.AuthorizationTemplate;
import uk.ac.ebi.biostd.authz.AuthzObject;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.authz.UserGroup;
import uk.ac.ebi.biostd.model.Submission;

public interface SecurityManager {

    void applyTemplate(AuthzObject gen, AuthorizationTemplate authorizationTemplate);

    boolean mayUserListAllSubmissions(User u);

    boolean mayUserReadSubmission(Submission sub, User user);

    boolean mayUserCreateSubmission(User usr);

    boolean mayUserUpdateSubmission(Submission oldSbm, User usr);

    boolean mayUserDeleteSubmission(Submission sbm, User usr);

    boolean mayUserAttachToSubmission(Submission s, User usr);

    boolean mayEveryoneReadSubmission(Submission submission);

    boolean mayUserCreateIdGenerator(User usr);

    User getUserById(long id);

    User getUserByLogin(String login);

    User getUserByEmail(String email);

    User getAnonymousUser();

    UserGroup getGroup(String name);

    boolean mayUserManageTags(User user);

    boolean mayUserReadGroupFiles(User user, UserGroup g);

    boolean mayUserWriteGroupFiles(User user, UserGroup group);

    boolean mayUserControlExport(User usr);

    boolean mayUserLockExport(User usr);

    int getUsersNumber();
}
