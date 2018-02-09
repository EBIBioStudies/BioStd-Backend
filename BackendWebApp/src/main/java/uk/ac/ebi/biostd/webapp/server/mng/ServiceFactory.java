package uk.ac.ebi.biostd.webapp.server.mng;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import uk.ac.ebi.biostd.webapp.server.mng.impl.SessionManagerImpl;

public class ServiceFactory {

    public static ServiceManager createService() throws ServiceInitExceprion {

        if (BackendConfig.getWorkDirectory() == null) {
            throw new ServiceInitExceprion("Service init error: work directory parameter is not defined");
        }

        Path wd = BackendConfig.getWorkDirectory();

        if (Files.exists(wd)) {
            if (!Files.isDirectory(wd)) {
                throw new ServiceInitExceprion(
                        "Service init error: work directory path '" + wd + "' should point to directory");
            }

            if (!Files.isWritable(wd)) {
                throw new ServiceInitExceprion("Service init error: work directory '" + wd + "' is not writable");
            }
        } else {
            try {
                Files.createDirectories(wd);
            } catch (IOException e) {
                throw new ServiceInitExceprion("Service init error: can't create work directory '" + wd + "'");
            }
        }

        Path sessDir = wd.resolve(ServiceConfig.SessionDir);

        if (Files.notExists(sessDir)) {
            try {
                Files.createDirectories(sessDir);
            } catch (IOException e) {
                throw new ServiceInitExceprion("Service init error: can't create session directory '" + sessDir + "'");
            }
        }

        ServiceManagerImpl serviceManager = new ServiceManagerImpl();
        serviceManager.setSessionManager(new SessionManagerImpl(sessDir.toFile()));
        serviceManager.setSubmissionManager(new JPASubmissionManager(BackendConfig.getEntityManagerFactory()));

        serviceManager.setSecurityManager(new SecurityManagerImpl());
        serviceManager.getSecurityManager().init();
        serviceManager.setUserManager(
                new JPAUserManager(serviceManager.getSecurityManager(), serviceManager.getSessionManager()));

        serviceManager.setFileManager(new FileManagerImpl());
        serviceManager.setReleaseManager(new JPAReleaser());
        serviceManager.setAccessionManager(new JPAAccessionManager());
        serviceManager.setTagManager(new JPATagManager());
        serviceManager.setSubscriptionManager(new JPASubscriptionManager());
        return serviceManager;
    }

}
