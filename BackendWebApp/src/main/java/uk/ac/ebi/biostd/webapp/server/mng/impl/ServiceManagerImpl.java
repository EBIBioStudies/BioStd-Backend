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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import uk.ac.ebi.biostd.webapp.server.email.EmailService;
import uk.ac.ebi.biostd.webapp.server.mng.AccessionManager;
import uk.ac.ebi.biostd.webapp.server.mng.FileManager;
import uk.ac.ebi.biostd.webapp.server.mng.ReleaseManager;
import uk.ac.ebi.biostd.webapp.server.mng.RemoteRequestManager;
import uk.ac.ebi.biostd.webapp.server.mng.ServiceConfig;
import uk.ac.ebi.biostd.webapp.server.mng.ServiceManager;
import uk.ac.ebi.biostd.webapp.server.mng.SubscriptionManager;
import uk.ac.ebi.biostd.webapp.server.mng.TagManager;
import uk.ac.ebi.biostd.webapp.server.mng.UserManager;
import uk.ac.ebi.biostd.webapp.server.mng.security.SecurityManager;


public class ServiceManagerImpl implements ServiceManager {

    private String serviceName;

    private ServiceConfig config;

    private UserManager userManager;
    private FileManager fileManager;
    private SecurityManager authzManager;
    private ReleaseManager releaser;
    private AccessionManager accManager;
    private EmailService emailService;
    private TagManager tagManager;
    private SubscriptionManager subscriptionManager;

    private static final ThreadLocal<EntityManager> threadLocal;

    static {
        threadLocal = new ThreadLocal<>();
    }

    private final EntityManagerFactory entityManagerFactory;


    public ServiceManagerImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Obtain a thread safe entity manager.
     *
     * @return an instance of {@link EntityManager} which provide entity manager.
     */
    @Override
    public EntityManager getEntityManager() {
        EntityManager em = threadLocal.get();

        if (em == null || !em.isOpen()) {
            threadLocal.set(entityManagerFactory.createEntityManager());
        }

        return threadLocal.get();
    }

    @Override
    public EmailService getEmailService() {
        return emailService;
    }

    @Override
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public ServiceConfig getConfiguration() {
        return config;
    }

    public void setConfiguration(ServiceConfig cfg) {
        config = cfg;
    }

    @Override
    public RemoteRequestManager getRemoteRequestManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileManager getFileManager() {
        return fileManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public SecurityManager getSecurityManager() {
        return authzManager;
    }

    public void setSecurityManager(SecurityManager authzManager) {
        this.authzManager = authzManager;
    }

    @Override
    public ReleaseManager getReleaseManager() {
        return releaser;
    }

    public void setReleaseManager(ReleaseManager releaser) {
        this.releaser = releaser;
    }

    @Override
    public AccessionManager getAccessionManager() {
        return accManager;
    }

    public void setAccessionManager(AccessionManager accManager) {
        this.accManager = accManager;
    }

    @Override
    public TagManager getTagManager() {
        return tagManager;
    }

    public void setTagManager(TagManager tagManager) {
        this.tagManager = tagManager;
    }

    @Override
    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }

    @Override
    public void setSubscriptionManager(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void shutdown() {
        userManager = null;
        fileManager = null;
        authzManager = null;
        releaser = null;
        accManager = null;
        emailService = null;
        tagManager = null;
    }
}
