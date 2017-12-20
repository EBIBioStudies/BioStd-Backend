package uk.ac.ebi.biostd.webapp.application.legacy;

import javax.servlet.ServletContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.config.ConfigurationManager;
import uk.ac.ebi.biostd.webapp.server.mng.SessionManager;

@Configuration
public class LegacyConfiguration {

    @Bean
    @DependsOn("appInitializer")
    public SessionManager serviceManager() {
        return BackendConfig.getServiceManager().getSessionManager();
    }

    @Bean
    public ConfigurationManager configurationManager(ServletContext context, Environment environment) {
        return new ConfigurationManager(context, environment);
    }

    @Bean()
    public ApplicationInitializer appInitializer(ServletContext context, ConfigurationManager manager) {
        return new ApplicationInitializer(context, manager);
    }
}
