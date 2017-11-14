package uk.ac.ebi.biostd.exporter;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.ac.ebi.biostd.exporter.configuration.QueryLoader;

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class ExporterApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ExporterApplication.class)
                .listeners(new QueryLoader())
                .run(args);
    }
}
