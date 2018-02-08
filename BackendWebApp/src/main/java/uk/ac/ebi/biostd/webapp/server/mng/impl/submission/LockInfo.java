package uk.ac.ebi.biostd.webapp.server.mng.impl.submission;

import java.util.Set;

class LockInfo {

    String lockOwner;
    Set<String> waiters;
}
