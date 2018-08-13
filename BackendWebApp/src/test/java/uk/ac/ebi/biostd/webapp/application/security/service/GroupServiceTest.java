package uk.ac.ebi.biostd.webapp.application.security.service;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserGroup;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.UserGroupRepository;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.UserRepository;

@RunWith(MockitoJUnitRunner.class)
public class GroupServiceTest {

    private static final long GROUP_ID = 50L;
    private static final long USER_ID = 44L;

    @Mock
    private UserGroupRepository mockUserGroupRepository;

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private MagicFolderUtil mockMagicFolderUtil;

    @InjectMocks
    private GroupService testInstance;

    private UserGroup userGroup = new UserGroup();

    @Before
    public void setup() {
        userGroup.setId(GROUP_ID);
    }

    @Test
    public void createGroup() {
        testInstance.createGroup(userGroup);

        verify(mockUserGroupRepository).save(userGroup);
        verify(mockMagicFolderUtil).createGroupMagicFolder(eq(GROUP_ID), anyString());
    }

    @Test
    public void getUsersGroups() {
        UserGroup userGroup = new UserGroup();
        User user = new User();
        user.setGroups(singleton(userGroup));

        when(mockUserRepository.getOne(USER_ID)).thenReturn(user);

        List<UserGroup> resultGroups = testInstance.getUsersGroups(USER_ID);
        assertThat(resultGroups).contains(userGroup);
    }
}
