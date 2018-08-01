package uk.ac.ebi.biostd.webapp.application.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
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
    @Mock private JwtParser mockJwtParser;
    @Mock private TokenUser testTokenUser;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private Jws<Claims> testJwsClaims;
    @Mock private Claims testClaims;

    @Before
    @SneakyThrows
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockJwtParser.setSigningKey(anyString())).thenReturn(mockJwtParser);
        when(mockJwtParser.parseClaimsJws(anyString())).thenReturn(testJwsClaims);
        when(testJwsClaims.getBody()).thenReturn(testClaims);
        when(testClaims.getSubject()).thenReturn("");
        securityUtil = spy(new SecurityUtil(mockObjectMapper, TEST_TOKEN_HASH, mockJwtParser));
    }

    @Test
    @SneakyThrows
    public void testCheckPasswordForSuperUser() {
        when(testTokenUser.isSuperuser()).thenReturn(true);
        when(testClaims.getSubject()).thenReturn(TEST_SUPER_USER_PAYLOAD);
        when(mockObjectMapper.readValue(TEST_SUPER_USER_PAYLOAD, TokenUser.class)).thenReturn(testTokenUser);
        assertThat(securityUtil.checkPassword(TEST_PASSWORD_DIGEST, TEST_SUPER_USER_TOKEN)).isTrue();
    }

    @Test
    @SneakyThrows
    public void testCheckPasswordForRegularUser() {
        byte[] regularUserPasswordDigest = securityUtil.getPasswordDigest(TEST_PASSWORD);

        when(testTokenUser.isSuperuser()).thenReturn(false);
        when(mockObjectMapper.readValue(anyString(), eq(TokenUser.class))).thenReturn(testTokenUser);
        assertThat(securityUtil.checkPassword(regularUserPasswordDigest, TEST_PASSWORD)).isTrue();
    }


    @Test
    @SneakyThrows
    public void testCheckPasswordForInvalidUser() {
        when(testTokenUser.isSuperuser()).thenReturn(false);
        when(mockObjectMapper.readValue(anyString(), eq(TokenUser.class))).thenReturn(testTokenUser);
        assertThat(securityUtil.checkPassword(TEST_PASSWORD_DIGEST, TEST_PASSWORD)).isFalse();
    }
}
