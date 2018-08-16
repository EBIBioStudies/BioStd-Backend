package uk.ac.ebi.biostd.webapp.application.security.service;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
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
import uk.ac.ebi.biostd.webapp.application.rest.exceptions.EntityNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class GroupServiceTest {
    private static final long GROUP_ID = 50L;
    private static final long USER_ID = 44L;
    private static final String GROUP_NAME = "Test Group";
    private static final String GROUP_SECRET = "abc";

    @Mock
    private UserGroupRepository mockUserGroupRepository;

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private MagicFolderUtil mockMagicFolderUtil;

    @InjectMocks
    private GroupService testInstance;

    private User user;
    private UserGroup userGroup;

    @Before
    public void setup() {
        user = new User();
        userGroup = new UserGroup();
        userGroup.setId(GROUP_ID);
        userGroup.setSecret(GROUP_SECRET);
        user.setGroups(singleton(userGroup));

        when(mockUserRepository.getOne(USER_ID)).thenReturn(user);
        when(mockUserGroupRepository.findByNameAndUsersContains(GROUP_NAME, user)).thenReturn(Optional.of(userGroup));
    }

    @Test
    public void createGroup() {
        testInstance.createGroup(userGroup);

        verify(mockUserGroupRepository).save(userGroup);
        verify(mockMagicFolderUtil).createGroupMagicFolder(eq(GROUP_ID), anyString());
    }

    @Test
    public void getUsersGroups() {
        List<UserGroup> resultGroups = testInstance.getUsersGroups(USER_ID);
        assertThat(resultGroups).contains(userGroup);
    }

    @Test
    public void getGroupFromUser() {
        UserGroup group = testInstance.getGroupFromUser(USER_ID, GROUP_NAME);
        assertThat(group).isEqualTo(userGroup);
    }

    @Test
    public void getNonExistingGroupFromUser() {
        assertThatExceptionOfType(
                EntityNotFoundException.class).isThrownBy(() -> testInstance.getGroupFromUser(USER_ID, ""));
    }

    @Test
    public void getGroupMagicFolderPat() {
        testInstance.getGroupMagicFolderPath(USER_ID, GROUP_NAME);

        verify(mockUserGroupRepository).findByNameAndUsersContains(GROUP_NAME, user);
        verify(mockMagicFolderUtil).getGroupMagicFolderPath(GROUP_ID, GROUP_SECRET);
    }
}
