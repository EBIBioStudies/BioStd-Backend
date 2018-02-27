package uk.ac.ebi.biostd.webapp.application.legacy.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.mng.SessionManager;
import uk.ac.ebi.biostd.webapp.server.mng.SubmissionManager;
import uk.ac.ebi.biostd.webapp.server.mng.UserManager;
import uk.ac.ebi.biostd.webapp.server.mng.security.SecurityManager;

@Configuration
@DependsOn("appInitializer")
public class LegacyBeansConfiguration {

    @Bean
    public SessionManager serviceManager() {
        return BackendConfig.getServiceManager().getSessionManager();
    }

    @Bean
    public UserManager userManager() {
        return BackendConfig.getServiceManager().getUserManager();
    }

    @Bean
    public SubmissionManager submissionManager() {
        return BackendConfig.getServiceManager().getSubmissionManager();
    }

    @Bean
    public SecurityManager securityManager() {
        return BackendConfig.getServiceManager().getSecurityManager();
    }
}
