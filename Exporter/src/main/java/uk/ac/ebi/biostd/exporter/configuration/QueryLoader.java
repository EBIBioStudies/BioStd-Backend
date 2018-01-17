package uk.ac.ebi.biostd.exporter.configuration;

import lombok.SneakyThrows;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.PropertySourcesLoader;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import uk.ac.ebi.biostd.exporter.persistence.Queries;

/**
 * Listen to application to {@link ApplicationEnvironmentPreparedEvent} then it read queries.xml so they become
 * available thought {@link Queries} file. Referenced in application.yml properties configuration file
 */
public class QueryLoader implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private final ResourceLoader loader = new DefaultResourceLoader();

    @Override
    @SneakyThrows
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Resource resource = loader.getResource("classpath:queries.xml");
        PropertySource<?> propertySource = new PropertySourcesLoader().load(resource);
        event.getEnvironment().getPropertySources().addLast(propertySource);
    }
}
