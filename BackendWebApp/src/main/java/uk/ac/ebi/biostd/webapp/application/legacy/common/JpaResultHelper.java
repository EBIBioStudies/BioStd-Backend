package uk.ac.ebi.biostd.webapp.application.legacy.common;

import javax.persistence.TypedQuery;

public class JpaResultHelper {

    public static <T> T getSingleResult(TypedQuery<T> query) {
        return query.getResultList().stream().findFirst().orElse(null);
    }
}
