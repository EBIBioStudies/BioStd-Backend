package uk.ac.ebi.biostd.webapp.application.security.rest.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Deprecated in favor of {@link LoginResponseDto}
 */
@Data
@Builder
@Deprecated
public class PermissionDto {

    private String status;
    private String allow;
    private String deny;
    private boolean superuser;
    private String name;
    private String login;
    private String email;
}
