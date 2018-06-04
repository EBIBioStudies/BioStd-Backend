package uk.ac.ebi.biostd.webapp.application.rest.dto;

import lombok.Data;

@Data
public class SubmissionDto {

    private String id;
    private String accno;
    private String title;
    private String ctime;
    private String mtime;
    private String rtime;
    private String version;
}
