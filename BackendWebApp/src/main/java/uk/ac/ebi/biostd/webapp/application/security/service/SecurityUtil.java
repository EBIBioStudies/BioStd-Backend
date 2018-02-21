package uk.ac.ebi.biostd.webapp.application.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pri.util.StringUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Arrays;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;

@Component
class SecurityUtil {

    private final ObjectMapper objectMapper;
    private final String tokenHash;
    private final MessageDigest sha1;

    SecurityUtil(ObjectMapper objectMapper, @Value("${biostudy.tokenHash}") String tokenHash)
            throws NoSuchAlgorithmException {
        this.objectMapper = objectMapper;
        this.tokenHash = tokenHash;
        this.sha1 = MessageDigest.getInstance("SHA1");
    }

    @SneakyThrows
    boolean checkPassword(byte[] passwordDigest, String password) {
        return Arrays.equals(sha1.digest(password.getBytes()), passwordDigest);
    }

    public byte[] getPasswordDigest(String password) {
        return sha1.digest(password.getBytes());
    }

    boolean checkHash(String hash, byte[] passwordDigest) {
        return hash.equalsIgnoreCase(StringUtils.toHexStr(passwordDigest));
    }

    @SneakyThrows
    TokenUser fromToken(String token) {
        String payload = Jwts.parser().setSigningKey(tokenHash).parseClaimsJws(token).getBody().getSubject();
        return objectMapper.readValue(payload, TokenUser.class);
    }

    @SneakyThrows
    String createToken(User user) {
        TokenUser tokenUser = TokenUser.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getEmail())
                .login(user.getLogin())
                .createTime(OffsetDateTime.now(Clock.systemUTC())).build();

        return Jwts.builder()
                .setSubject(objectMapper.writeValueAsString(tokenUser))
                .signWith(SignatureAlgorithm.HS512, tokenHash)
                .compact();
    }
}
