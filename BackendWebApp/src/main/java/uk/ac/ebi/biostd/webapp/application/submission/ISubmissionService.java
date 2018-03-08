package uk.ac.ebi.biostd.webapp.application.submission;

import java.util.List;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.Submission;

public interface ISubmissionService {

    /**
     * Obtain the list of active projects (submission with root section of type project) which user can access.
     *
     * @param userId the user unique identifier.
     * @param accessType the requested access type.
     * @return the list of project which user can access for the given access type.
     */
    List<Submission> getAllowedProjects(long userId, AccessType accessType);
}
