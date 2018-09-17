package uk.ac.ebi.biostd.exporter.jobs.full.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jobs.full.allSubmissions")
public class FullExportAllSubmissionsProperties extends FullExportFileProperties {
}
