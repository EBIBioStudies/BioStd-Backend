package uk.ac.ebi.biostd.webapp.application.security.services;

import static uk.ac.ebi.biostd.webapp.application.persitence.entities.Permission.READ_PERMISSION;
import static uk.ac.ebi.biostd.webapp.application.persitence.entities.Permission.SUBMIT_PERMISSION;

import com.google.common.base.MoreObjects;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.AccessTag;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.Submission;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserTagPermission;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.AccessTagsRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.SubmissionRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.UserRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.UserTagPermissionRepository;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.LoginInformation;

@Component
@AllArgsConstructor
public class AccessManager {

    private final UserRepository userRepository;
    private final UserTagPermissionRepository userTagPermissionRepository;
    private final SubmissionRepository submissionRepository;
    private final AccessTagsRepository tagsRepository;
    private final UsersManager userManager;

    public List<Submission> getAllowedProjects(long userId) {
        User user = userRepository.findOne(userId);
        List<AccessTag> accessTags = getAllowedTags(user, SUBMIT_PERMISSION);
        return submissionRepository.findProjectsByAccessTags(accessTags);
    }

    public Map<String, String> getPermissions(LoginInformation loginInformation) {
        User user = userManager.getUserLogin(loginInformation.getLogin(), loginInformation.getHash());

        Map<String, String> accessInfo = new LinkedHashMap<>(7);
        accessInfo.put("Status", "OK");
        accessInfo.put("Allow", getUserAllows(user));
        accessInfo.put("Deny", StringUtils.EMPTY);
        accessInfo.put("Superuser", String.valueOf(user.isSuperuser()));
        accessInfo.put("Name", user.getFullName());
        accessInfo.put("Login", MoreObjects.firstNonNull(user.getLogin(), StringUtils.EMPTY));
        accessInfo.put("EMail", user.getEmail());
        return accessInfo;
    }

    private List<AccessTag> getAllowedTags(User user, String permission) {
        if (user.isSuperuser()) {
            return tagsRepository.findAll();
        }

        return userTagPermissionRepository
                .findByUserIdAndPermissionName(user.getId(), permission).stream()
                .map(UserTagPermission::getAccessTag)
                .collect(Collectors.toList());
    }

    private String getUserAllows(User user) {
        List<String> accessTags = getAllowedTags(user, READ_PERMISSION)
                .stream()
                .map(AccessTag::getName)
                .collect(Collectors.toList());
        return "~" + user.getEmail() + ";#" + user.getId() + ";" + String.join(";", accessTags);
    }
}
