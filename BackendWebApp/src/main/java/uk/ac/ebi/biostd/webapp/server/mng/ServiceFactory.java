package uk.ac.ebi.biostd.webapp.server.mng;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.AllArgsConstructor;
import uk.ac.ebi.biostd.webapp.application.security.service.SecurityService;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.mng.exception.ServiceInitExceprion;
import uk.ac.ebi.biostd.webapp.server.mng.impl.FileManagerImpl;
import uk.ac.ebi.biostd.webapp.server.mng.impl.JPAAccessionManager;
import uk.ac.ebi.biostd.webapp.server.mng.impl.JPAReleaser;
import uk.ac.ebi.biostd.webapp.server.mng.impl.JPASubmissionManager;
import uk.ac.ebi.biostd.webapp.server.mng.impl.JPASubscriptionManager;
import uk.ac.ebi.biostd.webapp.server.mng.impl.JPATagManager;
import uk.ac.ebi.biostd.webapp.server.mng.impl.JPAUserManager;
import uk.ac.ebi.biostd.webapp.server.mng.impl.SecurityManagerImpl;
import uk.ac.ebi.biostd.webapp.server.mng.impl.ServiceManagerImpl;

@AllArgsConstructor
public class ServiceFactory {

    private final SecurityService securityService;

    public ServiceManager createService() throws ServiceInitExceprion {
        if (BackendConfig.getWorkDirectory() == null) {
            throw new ServiceInitExceprion("Service init error: work directory parameter is not defined");
        }

        Path workDirectory = BackendConfig.getWorkDirectory();

        if (Files.exists(workDirectory)) {
            if (!Files.isDirectory(workDirectory)) {
                throw new ServiceInitExceprion(
                        "Service init error: work directory path '" + workDirectory + "' should point to directory");
            }

            if (!Files.isWritable(workDirectory)) {
                throw new ServiceInitExceprion(
                        "Service init error: work directory '" + workDirectory + "' is not writable");
            }
        } else {
            try {
                Files.createDirectories(workDirectory);
            } catch (IOException e) {
                throw new ServiceInitExceprion(
                        "Service init error: can't create work directory '" + workDirectory + "'");
            }
        }

        Path sessDir = workDirectory.resolve(ServiceConfig.SessionDir);

        if (Files.notExists(sessDir)) {
            try {
                Files.createDirectories(sessDir);
            } catch (IOException e) {
                throw new ServiceInitExceprion("Service init error: can't create session directory '" + sessDir + "'");
            }
        }

        ServiceManagerImpl serviceManager = new ServiceManagerImpl((BackendConfig.getEntityManagerFactory()));
        serviceManager.setSubmissionManager(new JPASubmissionManager(BackendConfig.getEntityManagerFactory()));
        serviceManager.setSecurityManager(new SecurityManagerImpl(securityService));
        serviceManager.setUserManager(new JPAUserManager(serviceManager.getSecurityManager()));
        serviceManager.setFileManager(new FileManagerImpl());
        serviceManager.setReleaseManager(new JPAReleaser());
        serviceManager.setAccessionManager(new JPAAccessionManager());
        serviceManager.setTagManager(new JPATagManager());
        serviceManager.setSubscriptionManager(new JPASubscriptionManager());
        return serviceManager;
    }

}
