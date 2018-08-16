package uk.ac.ebi.biostd.webapp.application.security.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserGroup;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.UserGroupRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.UserRepository;
import uk.ac.ebi.biostd.webapp.application.rest.exceptions.EntityNotFoundException;

@Service
@AllArgsConstructor
@Transactional
public class GroupService {

    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;
    private final MagicFolderUtil magicFolderUtil;

    public UserGroup createGroup(UserGroup userGroup) {
        userGroup.setSecret(UUID.randomUUID().toString());
        userGroup.setDescription(userGroup.getDescription());
        userGroup.setProject(true);
        userGroupRepository.save(userGroup);
        magicFolderUtil.createGroupMagicFolder(userGroup.getId(), userGroup.getSecret());
        return userGroup;
    }

    public List<UserGroup> getUsersGroups(long userId) {
        return new ArrayList<>(userRepository.getOne(userId).getGroups());
    }

    public UserGroup getGroupFromUser(long userId, String groupName) {
        return userGroupRepository.findByNameAndUsersContains(groupName, userRepository.getOne(userId))
                .orElseThrow(() -> new EntityNotFoundException(String.format(
                        "The user is not associated to ghe group %s", groupName), UserGroup.class));
    }

    public Path getGroupMagicFolderPath(long userId, String groupName) {
        UserGroup group = getGroupFromUser(userId, groupName);
        return magicFolderUtil.getGroupMagicFolderPath(group.getId(), group.getSecret());
    }
}
