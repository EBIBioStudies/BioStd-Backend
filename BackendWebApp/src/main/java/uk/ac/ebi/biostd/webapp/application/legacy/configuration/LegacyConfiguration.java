package uk.ac.ebi.biostd.webapp.application.legacy.configuration;

import javax.servlet.ServletContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import uk.ac.ebi.biostd.webapp.application.security.service.SecurityService;
import uk.ac.ebi.biostd.webapp.server.config.ConfigurationManager;
import uk.ac.ebi.biostd.webapp.server.mng.ServiceFactory;

@Configuration
public class LegacyConfiguration {

    @Bean
    public ConfigurationManager configurationManager(ServletContext context, Environment environment,
            ServiceFactory serviceFactory) {
        return new ConfigurationManager(context, environment, serviceFactory);
    }

    @Bean
    public ApplicationInitializer appInitializer(ServletContext context, ConfigurationManager manager) {
        return new ApplicationInitializer(context, manager);
    }

    @Bean
    public ServiceFactory serviceFactory(SecurityService securityService) {
        return new ServiceFactory(securityService);
    }
}
