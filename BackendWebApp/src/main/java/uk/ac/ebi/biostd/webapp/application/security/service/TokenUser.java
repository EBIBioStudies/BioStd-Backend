package uk.ac.ebi.biostd.webapp.application.security.service;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenUser {

    private long id;
    private String email;
    private String fullName;
    private String login;
    private boolean superuser;
    private OffsetDateTime createTime;
}
