package uk.ac.ebi.biostd.webapp.application.security.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionDto {

    private String status;
    private String allow;
    private String deny;
    private boolean superuser;
    private String name;
    private String login;
    private String email;
}
