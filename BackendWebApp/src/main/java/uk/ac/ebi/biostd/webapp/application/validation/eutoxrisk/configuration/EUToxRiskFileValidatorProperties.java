package uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("eutoxrisk-file-validator")
public class EUToxRiskFileValidatorProperties {
    private boolean enabled = true;
    private String projectId = "EU-ToxRisk";
    private String endpoint = "https://eutoxrisk-validator.cloud.douglasconnect.com/v1/validate";
}
