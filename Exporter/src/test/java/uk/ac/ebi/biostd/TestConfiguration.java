package uk.ac.ebi.biostd;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import uk.ac.ebi.biostd.exporter.configuration.GeneralConfiguration;
import uk.ac.ebi.biostd.exporter.jobs.JobsPipelinesConfiguration;
import uk.ac.ebi.biostd.exporter.persistence.Queries;
import uk.ac.ebi.biostd.exporter.rest.NotificationController;
import uk.ac.ebi.biostd.exporter.service.SubmissionService;

@Configuration
@Import(GeneralConfiguration.class)
@ComponentScan(basePackageClasses = {
        JobsPipelinesConfiguration.class,
        Queries.class,
        NotificationController.class,
        SubmissionService.class})
@EnableAutoConfiguration
public class TestConfiguration {

}
