package uk.ac.ebi.biostd.webapp.server.util;

import javax.persistence.EntityTransaction;
import lombok.experimental.UtilityClass;

/**
 * Helps with common database operations.
 */
@UtilityClass
public class DatabaseUtil {

    public void commitIfActiveAndNotNull(EntityTransaction transaction) {
        if (transaction != null && transaction.isActive()) {
            transaction.commit();
        }
    }
}
