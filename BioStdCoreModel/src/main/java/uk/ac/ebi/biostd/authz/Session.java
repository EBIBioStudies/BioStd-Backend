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

import java.io.File;
import javax.persistence.EntityManager;

public interface Session {

    User getUser();

    void setUser(User user);

    String getSSOToken();

    void setSSOToken(String ssoToken);

    long getLastAccessTime();

    void setLastAccessTime(long lastAccessTime);

    String getSessionKey();

    void setSessionKey(String sessionKey);

    File makeTempFile();

    void destroy();

    boolean isCheckedIn();

    void setCheckedIn(boolean checkIn);

    boolean isAnonymouns();

    EntityManager getEntityManager();

}