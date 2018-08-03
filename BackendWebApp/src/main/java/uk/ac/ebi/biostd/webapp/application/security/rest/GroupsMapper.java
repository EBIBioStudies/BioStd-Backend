package uk.ac.ebi.biostd.webapp.application.security.rest;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserGroup;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.UserRepository;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.UserGroupDto;

@AllArgsConstructor
@Component
class GroupsMapper {

    private final UserRepository userRepository;

    UserGroup toGroup(UserGroupDto userGroupDto) {
        UserGroup group = new UserGroup();
        group.setOwner(userRepository.getOne(userGroupDto.getOwnerId()));
        group.setName(userGroupDto.getName());
        group.setDescription(userGroupDto.getDescription());

        return group;
    }

    UserGroupDto toDto(UserGroup group) {
        UserGroupDto groupDto = new UserGroupDto();
        groupDto.setName(group.getName());
        groupDto.setDescription(group.getDescription());
        groupDto.setOwnerId(group.getOwner().getId());
        return groupDto;
    }
}
