package uk.ac.ebi.biostd.webapp.application.security.service;

import java.util.UUID;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserGroup;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.UserGroupRepository;

@Service
@AllArgsConstructor
public class GroupService {

    private final UserGroupRepository userGroupRepository;
    private final MagicFolderUtil magicFolderUtil;

    @Transactional
    public UserGroup createGroup(UserGroup userGroup) {
        userGroup.setSecret(UUID.randomUUID().toString());
        userGroup.setDescription(userGroup.getDescription());
        userGroup.setProject(true);
        userGroupRepository.save(userGroup);
        magicFolderUtil.createGroupMagicFolder(userGroup.getId(), userGroup.getSecret());
        return userGroup;
    }
}
