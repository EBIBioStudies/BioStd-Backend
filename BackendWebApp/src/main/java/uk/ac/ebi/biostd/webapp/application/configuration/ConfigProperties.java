package uk.ac.ebi.biostd.webapp.application.configuration;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.ac.ebi.biostd.webapp.server.config.ConfigurationManager.BIOSTUDY_BASE_DIR;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;
import javax.servlet.ServletContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ConfigProperties {

    private static final String CONFIG_FILE_LOCATION_VAR = "biostudy.configFile";

    private final Properties properties;
    private final Environment environment;

    public ConfigProperties(Environment environment, ServletContext servletContext) throws IOException {
        this.environment = environment;
        this.properties = new Properties();

        loadProperties(servletContext);
    }

    public String get(String property) {
        String propertyVal = properties.getProperty(property);
        propertyVal = defaultIfNull(propertyVal, environment.getProperty(property));

        return Preconditions.checkNotNull(propertyVal, property + " property could not be found");
    }

    private void loadProperties(ServletContext context) throws IOException {
        File config = Paths.get(getConfigFileLocation()).toFile();

        if (config.exists()) {
            properties.load(new FileInputStream(config));
            return;
        }

        Collections.list(context.getInitParameterNames())
                .forEach(key -> properties.setProperty(key, context.getInitParameter(key)));
    }

    private String getConfigFileLocation() {
        String baseDir = environment.getProperty(BIOSTUDY_BASE_DIR);
        return Strings.isNullOrEmpty(baseDir) ?
                environment.getProperty(CONFIG_FILE_LOCATION_VAR) :
                baseDir + "/config.properties";
    }
}
