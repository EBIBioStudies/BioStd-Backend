package uk.ac.ebi.biostd.webapp.server.security;

import java.io.File;
import javax.persistence.EntityManager;
import uk.ac.ebi.biostd.authz.User;

public interface Session {

    User getUser();

    long getLastAccessTime();

    File makeTempFile();

    void destroy();

    boolean isAnonymous();

    EntityManager getEntityManager();

}