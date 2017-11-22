package uk.ac.ebi.biostd.backend.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.ac.ebi.biostd.backend.parsing.Triplet;

@Getter
@Setter
@Builder
public class LoginResponse {

    private String status;
    private String sessid;
    private String username;
    private String email;
    private boolean superuser;
    private String dropbox;
    private String ssotoken;

    public static LoginResponse fromList(List<Triplet> triplets) {
        return LoginResponse.builder()
                .status(triplets.get(0).getKey())
                .sessid(triplets.get(1).getFirstValue())
                .username(triplets.get(2).getFirstValue())
                .email(triplets.get(3).getFirstValue())
                .superuser(triplets.get(4).getFirstValueAsBoolean())
                .dropbox(triplets.get(5).getFirstValue())
                .ssotoken(triplets.get(6).getFirstValue())
                .build();
    }
}
