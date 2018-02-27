package uk.ac.ebi.biostd.webapp.server.security;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.biostd.authz.User;

@Slf4j
public class SessionAuthenticated implements Session {

    private final File sessionDir;
    private final EntityManagerFactory managerFactory;

    private final User user;
    private final long lastAccessTime;
    private final AtomicInteger tmpFileCounter = new AtomicInteger(0);

    public SessionAuthenticated(File sessDir, EntityManagerFactory managerFactory, User user) {
        sessionDir = sessDir;
        this.user = user;
        lastAccessTime = System.currentTimeMillis();
        this.managerFactory = managerFactory;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public long getLastAccessTime() {
        return lastAccessTime;
    }

    @Override
    public File makeTempFile() {
        if (!sessionDir.exists()) {
            if (!sessionDir.mkdirs()) {
                log.error("Can't create session directory: " + sessionDir.getAbsolutePath());
            }
        }

        return new File(sessionDir, String.valueOf(tmpFileCounter.incrementAndGet()));
    }

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
    }

    @Override
    public EntityManager getEntityManager() {
        return managerFactory.createEntityManager();
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }
}
