package uk.ac.ebi.biostd.webapp.application.submission;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessPermission.AccessType;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessTag;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.Submission;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.AccessPermissionRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.AccessTagsRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.SubmissionRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.UserRepository;

@Service
@AllArgsConstructor
public class SubmissionService implements ISubmissionService {

    private final AccessPermissionRepository permissionsRepository;
    private final AccessTagsRepository tagsRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    @Override
    public List<Submission> getAllowedProjects(long userId, AccessType accessType) {
        User user = userRepository.findOne(userId);
        List<AccessTag> accessTags = getAllowedTags(user, accessType);

        return accessTags.stream()
                .map(submissionRepository::findProjectByAccessTag)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private List<AccessTag> getAllowedTags(User user, AccessType accessType) {
        if (user.isSuperuser()) {
            return tagsRepository.findAll();
        }

        return permissionsRepository.findByUserIdAndAccessType(user.getId(), accessType).stream()
                .map(AccessPermission::getAccessTag)
                .collect(Collectors.toList());
    }
}
