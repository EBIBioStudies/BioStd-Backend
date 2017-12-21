package uk.ac.ebi.biostd.webapp.application.configuration;

import com.google.common.base.Preconditions;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import javax.servlet.ServletContext;
import org.apache.commons.lang3.StringUtils;
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
        return Preconditions.checkNotNull(properties.getProperty(property), property + " property could not be found");
    }

    private void loadProperties(ServletContext context) throws IOException {
        String config = environment.getProperty(CONFIG_FILE_LOCATION_VAR);

        if (StringUtils.isNotBlank(config)) {
            properties.load(new FileInputStream(config));
            return;
        }

        Collections.list(context.getInitParameterNames())
                .forEach(key -> properties.setProperty(key, context.getInitParameter(key)));
    }
}
