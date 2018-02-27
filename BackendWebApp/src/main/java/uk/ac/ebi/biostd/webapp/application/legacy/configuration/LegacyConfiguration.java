package uk.ac.ebi.biostd.webapp.application.legacy.configuration;

import javax.servlet.ServletContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import uk.ac.ebi.biostd.webapp.server.config.ConfigurationManager;

@Configuration
public class LegacyConfiguration {

    @Bean
    public ConfigurationManager configurationManager(ServletContext context, Environment environment) {
        return new ConfigurationManager(context, environment);
    }

    @Bean
    public ApplicationInitializer appInitializer(ServletContext context, ConfigurationManager manager) {
        return new ApplicationInitializer(context, manager);
    }
}
