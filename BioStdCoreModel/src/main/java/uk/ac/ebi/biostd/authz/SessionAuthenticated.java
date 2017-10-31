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
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionAuthenticated implements Session {


    private static Logger log;
    private final File sessionDir;
    private final Map<Thread, EntityManager> thrEMMap = new HashMap<>();
    private final EntityManagerFactory emf;
    private String sessionKey;
    private User user;
    private String ssoToken = null;
    private long lastAccessTime;
    private volatile int checkedIn = 0;
    private int tmpFileCounter = 0;
    private EntityManager defaultEM;

    public SessionAuthenticated(File sessDir, EntityManagerFactory fct) {
        if (log == null) {
            log = LoggerFactory.getLogger(getClass());
        }

        sessionDir = sessDir;
        lastAccessTime = System.currentTimeMillis();

        emf = fct;
    }

    /* (non-Javadoc)
     * @see uk.ac.ebi.biostd.authz.SessionIF#getUser()
     */
    @Override
    public User getUser() {
        return user;
    }

    /* (non-Javadoc)
     * @see uk.ac.ebi.biostd.authz.SessionIF#setUser(uk.ac.ebi.biostd.authz.User)
     */
    @Override
    public void setUser(User user) {
        this.user = user;
    }

    /* (non-Javadoc)
     * @see uk.ac.ebi.biostd.authz.SessionIF#getSSOToken()
     */
    @Override
    public String getSSOToken() {
        return ssoToken;
    }

    /* (non-Javadoc)
     * @see uk.ac.ebi.biostd.authz.SessionIF#setSSOToken(String)
     */
    @Override
    public void setSSOToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    /* (non-Javadoc)
     * @see uk.ac.ebi.biostd.authz.SessionIF#getLastAccessTime()
     */
    @Override
    public long getLastAccessTime() {
        return lastAccessTime;
    }

    /* (non-Javadoc)
     * @see uk.ac.ebi.biostd.authz.SessionIF#setLastAccessTime(long)
     */
    @Override
    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    /* (non-Javadoc)
     * @see uk.ac.ebi.biostd.authz.SessionIF#getSessionKey()
     */
    @Override
    public String getSessionKey() {
        return sessionKey;
    }

    /* (non-Javadoc)
     * @see uk.ac.ebi.biostd.authz.SessionIF#setSessionKey(java.lang.String)
     */
    @Override
    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    /* (non-Javadoc)
     * @see uk.ac.ebi.biostd.authz.SessionIF#makeTempFile()
     */
    @Override
    public File makeTempFile() {
        if (!sessionDir.exists()) {
            if (!sessionDir.mkdirs()) {
                log.error("Can't create session directory: " + sessionDir.getAbsolutePath());
            }
        }

        return new File(sessionDir, String.valueOf(++tmpFileCounter));
    }


    /* (non-Javadoc)
     * @see uk.ac.ebi.biostd.authz.SessionIF#destroy()
     */
    @Override
    public void destroy() {
        if (sessionDir != null && sessionDir.exists()) {
            for (File f : sessionDir.listFiles()) {
                if (!f.delete()) {
                    log.error("Can't delete session file: " + f.getAbsolutePath());
                }
            }

            if (!sessionDir.delete()) {
                log.error("Can't delete session directory: " + sessionDir.getAbsolutePath());
            }
        }

        synchronized (thrEMMap) {
            if (defaultEM != null) {
                defaultEM.close();
            }

            for (EntityManager em : thrEMMap.values()) {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }

    }

    /* (non-Javadoc)
     * @see uk.ac.ebi.biostd.authz.SessionIF#isCheckedIn()
     */
    @Override
    public boolean isCheckedIn() {
        return checkedIn > 0;
    }

    /* (non-Javadoc)
     * @see uk.ac.ebi.biostd.authz.SessionIF#setCheckedIn(boolean)
     */
    @Override
    public void setCheckedIn(boolean checkIn) {
        lastAccessTime = System.currentTimeMillis();

        synchronized (thrEMMap) {
            EntityManager em = thrEMMap.remove(Thread.currentThread());

            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        if (checkIn) {
            checkedIn++;
        } else {
            checkedIn--;
        }

    }


    @Override
    public EntityManager getEntityManager() {
        synchronized (thrEMMap) {
            EntityManager em = thrEMMap.get(Thread.currentThread());

            if (em != null && em.isOpen()) {
                return em;
            }

            em = emf.createEntityManager();
            thrEMMap.put(Thread.currentThread(), em);
            return em;

        }

    }

    public EntityManager getEntityManagerOld() {
        synchronized (thrEMMap) {
            EntityManager em = thrEMMap.get(Thread.currentThread());

            if (em != null && em.isOpen()) {
                return em;
            }

            if (defaultEM != null && defaultEM.isOpen()) {
                em = defaultEM;
                defaultEM = null; //taken or invalid

                thrEMMap.put(Thread.currentThread(), em);
                return em;
            }

            defaultEM = null; //may be invalid

            em = emf.createEntityManager();
            thrEMMap.put(Thread.currentThread(), em);
            return em;

        }

    }

    @Override
    public boolean isAnonymouns() {
        return false;
    }

}
