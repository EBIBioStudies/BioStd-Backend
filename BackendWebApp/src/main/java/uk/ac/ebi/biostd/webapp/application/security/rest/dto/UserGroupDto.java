package uk.ac.ebi.biostd.webapp.application.security.rest.dto;

import lombok.Data;

@Data
public class UserGroupDto {

    private String description;
    private String name;
    private long ownerId;
    private long groupId;
}
