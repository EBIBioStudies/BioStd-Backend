package uk.ac.ebi.biostd.webapp.application.security.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserGroup;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.UserRepository;
import uk.ac.ebi.biostd.webapp.application.security.rest.dto.UserGroupDto;

@RunWith(MockitoJUnitRunner.class)
public class GroupsMapperTest {

    private static final String GROUP_NAME = "a group";
    private static final long GROUP_ID = 24L;
    private static final String GROUP_DESCRIPTION = "a group description";
    private static final long OWNER_ID = 10L;

    @Mock
    private UserRepository mockUserRepository;

    @InjectMocks
    private GroupsMapper testInstance;

    private final User owner = new User();

    @Before
    public void setup() {
        when(mockUserRepository.getOne(OWNER_ID)).thenReturn(owner);
        owner.setId(OWNER_ID);
    }

    @Test
    public void toGroup() {
        UserGroup result = testInstance.toGroup(createGroupDto());
        assertThat(result.getDescription()).isEqualTo(GROUP_DESCRIPTION);
        assertThat(result.getName()).isEqualTo(GROUP_NAME);
        assertThat(result.getOwner()).isEqualTo(owner);
    }

    @Test
    public void toDto() {
        UserGroupDto resultDto = testInstance.toDto(createGroup());
        assertThat(resultDto.getDescription()).isEqualTo(GROUP_DESCRIPTION);
        assertThat(resultDto.getName()).isEqualTo(GROUP_NAME);
        assertThat(resultDto.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(resultDto.getGroupId()).isEqualTo(GROUP_ID);
    }

    @Test
    public void toDtoList() {
        List<UserGroupDto> results = testInstance.toDtoList(Collections.singletonList(createGroup()));
        assertThat(results).hasSize(1);

        UserGroupDto result = results.get(0);
        assertThat(result.getDescription()).isEqualTo(GROUP_DESCRIPTION);
        assertThat(result.getName()).isEqualTo(GROUP_NAME);
        assertThat(result.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(result.getGroupId()).isEqualTo(GROUP_ID);
    }

    private UserGroupDto createGroupDto() {
        UserGroupDto userGroup = new UserGroupDto();
        userGroup.setName(GROUP_NAME);
        userGroup.setOwnerId(OWNER_ID);
        userGroup.setDescription(GROUP_DESCRIPTION);
        return userGroup;
    }

    private UserGroup createGroup() {
        UserGroup userGroup = new UserGroup();
        userGroup.setName(GROUP_NAME);
        userGroup.setDescription(GROUP_DESCRIPTION);
        userGroup.setOwner(owner);
        userGroup.setId(GROUP_ID);
        return userGroup;
    }
}
