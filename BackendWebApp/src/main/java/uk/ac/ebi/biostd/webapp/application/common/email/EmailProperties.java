package uk.ac.ebi.biostd.webapp.application.common.email;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
public class EmailProperties {

    private String from;
    private String subject;
}
