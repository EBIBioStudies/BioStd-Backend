package uk.ac.ebi.biostd.webapp.application.security.rest;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserGroup;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.UserGroupDto;
import uk.ac.ebi.biostd.webapp.application.security.service.GroupService;

@RestController
@RequestMapping("/groups")
@AllArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final GroupsMapper groupsMapper;

    @PostMapping
    public UserGroupDto createGroup(@RequestBody UserGroupDto userGroupDto) {
        UserGroup group = groupService.createGroup(groupsMapper.toGroup(userGroupDto));
        return groupsMapper.toDto(group);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<UserGroupDto> getGroups(@AuthenticationPrincipal uk.ac.ebi.biostd.authz.User user) {
        return groupsMapper.toDtoList(groupService.getUsersGroups(user.getId()));
    }
}
