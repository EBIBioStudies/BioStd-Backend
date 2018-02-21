package uk.ac.ebi.biostd.webapp.application.security.rest.model;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.Data;

@Data
public class UserData {

    private String token;
    private String username;
    private String email;
    private String userAux;
    private boolean superUser;

    public Map<String, String> asMap() {
        return ImmutableMap.<String, String>builder()
                .put("sessid", token)
                .put("login", username)
                .put("email", email)
                .put("aux", userAux)
                .put("superuser", String.valueOf(superUser)).build();
    }
}
