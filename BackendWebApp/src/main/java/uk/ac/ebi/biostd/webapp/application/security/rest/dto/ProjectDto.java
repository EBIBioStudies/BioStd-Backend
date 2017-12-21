package uk.ac.ebi.biostd.webapp.application.security.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectDto {

    private long id;
    private String accno;
    private String rstitle;
    private String title;
    private long rtime;
    private long ctime;
    private long mtime;
    private String type;
    private long version;
}
