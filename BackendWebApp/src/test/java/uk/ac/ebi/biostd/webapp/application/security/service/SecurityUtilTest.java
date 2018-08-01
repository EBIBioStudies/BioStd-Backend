package uk.ac.ebi.biostd.webapp.application.security.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SecurityUtilTest {
    private static final String TEST_PASSWORD = "123456";
    private static final String TEST_TOKEN_HASH = "biostd";
    private static final byte[] TEST_PASSWORD_DIGEST = TEST_PASSWORD.getBytes();
    private static final String TEST_SUPER_USER_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ7XG4gIFwiaWRcIiA6IDMsXG4gIFwi";
    private static final String TEST_SUPER_USER_PAYLOAD = "ZVwiIDogXCJhZG1pbl91c2VyQGViaS5hYy51a1wiLFxuICBcImxvZ2luXCd";

    private SecurityUtil securityUtil;
    @Mock private TokenUser testTokenUser;
    @Mock private ObjectMapper mockObjectMapper;

    @Before
    @SneakyThrows
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        securityUtil = spy(new SecurityUtil(mockObjectMapper, TEST_TOKEN_HASH));
    }

    @Test
    @SneakyThrows
    public void testCheckPasswordForSuperUser() {
        when(testTokenUser.isSuperuser()).thenReturn(true);
        doReturn(TEST_SUPER_USER_PAYLOAD).when(securityUtil).getSerializedTokenUser(TEST_SUPER_USER_TOKEN);
        when(mockObjectMapper.readValue(TEST_SUPER_USER_PAYLOAD, TokenUser.class)).thenReturn(testTokenUser);
        assertTrue(
                "Super admin user should be authenticated",
                securityUtil.checkPassword(TEST_PASSWORD_DIGEST, TEST_SUPER_USER_TOKEN));
    }

    @Test
    @SneakyThrows
    public void testCheckPasswordForRegularUser() {
        byte[] regularUserPasswordDigest = securityUtil.getPasswordDigest(TEST_PASSWORD);

        when(testTokenUser.isSuperuser()).thenReturn(false);
        when(mockObjectMapper.readValue(anyString(), eq(TokenUser.class))).thenReturn(testTokenUser);
        assertTrue(
                "Regular user should be authenticated",
                securityUtil.checkPassword(regularUserPasswordDigest, TEST_PASSWORD));
    }


    @Test
    @SneakyThrows
    public void testCheckPasswordForInvalidUser() {
        when(testTokenUser.isSuperuser()).thenReturn(false);
        when(mockObjectMapper.readValue(anyString(), eq(TokenUser.class))).thenReturn(testTokenUser);
        assertFalse(
                "Regular user should be authenticated",
                securityUtil.checkPassword(TEST_PASSWORD_DIGEST, TEST_PASSWORD));
    }
}
