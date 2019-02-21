package uk.ac.ebi.biostd.webapp.application.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pri.util.StringUtils;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;

@Component
@Slf4j
class SecurityUtil {

    private final ObjectMapper objectMapper;
    private final String tokenHash;
    private final MessageDigest sha1;
    private final JwtParser jwtParser;

    SecurityUtil(ObjectMapper objectMapper, @Value("${biostudy.tokenHash}") String tokenHash, JwtParser jwtParser)
            throws NoSuchAlgorithmException {
        this.objectMapper = objectMapper;
        this.tokenHash = tokenHash;
        this.jwtParser = jwtParser;
        this.sha1 = MessageDigest.getInstance("SHA1");
    }

    @SneakyThrows
    boolean checkPassword(byte[] passwordDigest, String password) {
        Optional<TokenUser> tokenUser = fromToken(password);
        boolean isValidSuperUser = tokenUser.isPresent() && tokenUser.get().isSuperuser();
        boolean isValidRegularUser = Arrays.equals(getPasswordDigest(password), passwordDigest);

        return isValidSuperUser || isValidRegularUser;
    }

    byte[] getPasswordDigest(String password) {
        return sha1.digest(password.getBytes());
    }

    boolean checkHash(String hash, byte[] passwordDigest) {
        return hash.equalsIgnoreCase(StringUtils.toHexStr(passwordDigest));
    }

    @SneakyThrows
    Optional<TokenUser> fromToken(String token) {
        Optional<TokenUser> tokenUser = Optional.empty();

        try {
            String payload = jwtParser.setSigningKey(tokenHash).parseClaimsJws(token).getBody().getSubject();
            tokenUser = Optional.of(objectMapper.readValue(payload, TokenUser.class));
        } catch (SignatureException | MalformedJwtException exception) {
            log.error("detected invalid signature token");
        }

        return tokenUser;
    }

    @SneakyThrows
    String createToken(User user) {
        TokenUser tokenUser = TokenUser.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getEmail())
                .login(user.getLogin())
                .superuser(user.isSuperuser())
                .createTime(OffsetDateTime.now(Clock.systemUTC())).build();

        return Jwts.builder()
                .setSubject(objectMapper.writeValueAsString(tokenUser))
                .signWith(SignatureAlgorithm.HS512, tokenHash)
                .compact();
    }

    // TODO: Extract instance in safe place and disable using custom url.
    String getInstanceUrl(String instanceKey, String path) {
        switch (instanceKey) {
            // DEV
            case "975dd2ca-58eb-407b-ba0f-858f15f7304d":
                return "http://ves-hx-f2.ebi.ac.uk:8120" + path;
            // BETA
            case "9c584ae3-678a-4462-b685-54c37a1bc047":
                return "https://wwwdev.ebi.ac.uk/biostudies" + path;
            // PROD
            case "01ecc118-dbec-4df8-8fe8-f5cd7364b2b7":
                return "https://www.ebi.ac.uk/biostudies" + path;

            default:
                if (instanceKey.startsWith("http://localhost:") ||
                        instanceKey.startsWith("https://localhost:")) {
                    return instanceKey + path;
                }

                throw new IllegalArgumentException(String.format("invalid instance key '%s'", instanceKey));
        }
    }
}
