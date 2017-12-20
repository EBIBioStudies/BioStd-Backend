package uk.ac.ebi.biostd.webapp.application.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.servlet.ServletContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ConfigProperties {

    private static final String NOT_VALUE_STRING = "<NONE>";
    private static final String ENVIRONMENT_VAR = "biostudy.base.dir";
    private static final String INIT_PARAMETER = "contextConfigLocation";

    private final Properties properties;
    private final Environment environment;

    public ConfigProperties(ServletContext servletContext, Environment environment) throws IOException {
        this.environment = environment;
        this.properties = new Properties();

        String config = getConfigFilePath(servletContext);
        this.properties.load(new FileInputStream(config));
    }

    public String get(String property) {
        return properties.getProperty(property);
    }

    private String getConfigFilePath(ServletContext servletContext) {
        String config = servletContext.getInitParameter(INIT_PARAMETER);

        if (StringUtils.isNotBlank(config) && !config.equals(NOT_VALUE_STRING)) {
            return config;
        }

        config = environment.getProperty(ENVIRONMENT_VAR);
        if (StringUtils.isNotBlank(config)) {
            return config + "/config.properties";
        }

        throw new IllegalStateException("Could not find config file parameter or environment variable");
    }
}
