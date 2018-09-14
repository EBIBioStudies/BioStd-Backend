package uk.ac.ebi.biostd.exporter.persistence.model;

import lombok.Data;

@Data
public class SubAndUserInfo {

    private long subId;
    private String subTitle;
    private String subAccNo;
    private long subReleaseTime;
    private String authorFullName;
    private String authorEmail;
}
