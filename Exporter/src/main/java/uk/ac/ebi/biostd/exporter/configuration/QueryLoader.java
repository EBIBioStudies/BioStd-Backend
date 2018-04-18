package uk.ac.ebi.biostd.exporter.configuration;

import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import uk.ac.ebi.biostd.exporter.persistence.Queries;

/**
 * Listen to application to {@link ApplicationEnvironmentPreparedEvent} then it read queries.xml so they become
 * available thought {@link Queries} file. Referenced in application.yml properties configuration file
 */
public class QueryLoader implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final List<String> RESOURCES = Arrays.asList("classpath:queries.xml", "classpath:aux_queries.xml");

    private final ResourceLoader loader = new DefaultResourceLoader();

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        MutablePropertySources sources = event.getEnvironment().getPropertySources();

        RESOURCES.forEach(path -> loadResource(sources, path));
    }

    @SneakyThrows
    private void loadResource(MutablePropertySources sources, String path) {
        Resource resource = loader.getResource(path);
        new PropertiesPropertySourceLoader().load(resource.getFilename(), resource).forEach(sources::addLast);
    }
}
