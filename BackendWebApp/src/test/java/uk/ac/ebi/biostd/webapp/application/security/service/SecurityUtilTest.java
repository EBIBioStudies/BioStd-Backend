package uk.ac.ebi.biostd.webapp.application.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SecurityUtilTest {
    private static final String TEST_PASSWORD = "123456";
    private static final String TEST_TOKEN_HASH = "biostd";
    private static final byte[] TEST_PASSWORD_DIGEST = TEST_PASSWORD.getBytes();
    private static final String TEST_SUPER_USER_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ7XG4gIFwiaWRcIiA6IDMsXG4gIFwi";
    private static final String TEST_SUPER_USER_PAYLOAD = "ZVwiIDogXCJhZG1pbl91c2VyQGViaS5hYy51a1wiLFxuICBcImxvZ2luXCd";

    @Mock
    private JwtParser mockJwtParser;

    @Mock
    private TokenUser testTokenUser;

    @Mock
    private ObjectMapper mockObjectMapper;

    @Mock
    private Jws<Claims> testJwsClaims;

    @Mock
    private Claims testClaims;

    private SecurityUtil testInstance;

    @Before
    public void setUp() throws Exception {
        when(mockJwtParser.setSigningKey(anyString())).thenReturn(mockJwtParser);
        when(mockJwtParser.parseClaimsJws(anyString())).thenReturn(testJwsClaims);
        when(testJwsClaims.getBody()).thenReturn(testClaims);
        when(testClaims.getSubject()).thenReturn("");

        testInstance = new SecurityUtil(mockObjectMapper, TEST_TOKEN_HASH, mockJwtParser);
    }

    @Test
    public void testCheckPasswordForSuperUser() throws Exception {
        when(testTokenUser.isSuperuser()).thenReturn(true);
        when(testClaims.getSubject()).thenReturn(TEST_SUPER_USER_PAYLOAD);
        when(mockObjectMapper.readValue(TEST_SUPER_USER_PAYLOAD, TokenUser.class)).thenReturn(testTokenUser);
        assertThat(testInstance.checkPassword(TEST_PASSWORD_DIGEST, TEST_SUPER_USER_TOKEN)).isTrue();
    }

    @Test
    public void testCheckPasswordForRegularUser() throws Exception {
        byte[] regularUserPasswordDigest = testInstance.getPasswordDigest(TEST_PASSWORD);

        when(testTokenUser.isSuperuser()).thenReturn(false);
        when(mockObjectMapper.readValue(anyString(), eq(TokenUser.class))).thenReturn(testTokenUser);
        assertThat(testInstance.checkPassword(regularUserPasswordDigest, TEST_PASSWORD)).isTrue();
    }

    @Test
    public void testCheckPasswordForInvalidUser() throws Exception {
        when(testTokenUser.isSuperuser()).thenReturn(false);
        when(mockObjectMapper.readValue(anyString(), eq(TokenUser.class))).thenReturn(testTokenUser);
        assertThat(testInstance.checkPassword(TEST_PASSWORD_DIGEST, TEST_PASSWORD)).isFalse();
    }
}
