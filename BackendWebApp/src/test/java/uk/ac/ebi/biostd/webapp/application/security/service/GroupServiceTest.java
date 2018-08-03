package uk.ac.ebi.biostd.webapp.application.security.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserGroup;
import uk.ac.ebi.biostd.webapp.application.persitence.repositories.UserGroupRepository;

@RunWith(MockitoJUnitRunner.class)
public class GroupServiceTest {

    private static final long GROUP_ID = 50L;

    @Mock
    private UserGroupRepository mockUserGroupRepository;

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
}
