package uk.ac.ebi.biostd.webapp.application.legacy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import lombok.AllArgsConstructor;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.config.ConfigurationException;
import uk.ac.ebi.biostd.webapp.server.config.ConfigurationManager;

@AllArgsConstructor
public class ApplicationInitializer {

    private final ServletContext servletContext;
    private final ConfigurationManager configurationManager;

    @PostConstruct
    public void init() throws ConfigurationException {
        BackendConfig.setInstanceId(servletContext.getContextPath().hashCode());
        BackendConfig.setConfigurationManager(configurationManager);
        BackendConfig.getConfigurationManager().loadConfiguration();
        BackendConfig.setConfigValid(true);
    }

    @PreDestroy
    public void destroy() {
        if (BackendConfig.isConfigValid()) {
            BackendConfig.getConfigurationManager().stopServices();
        }
    }
}
