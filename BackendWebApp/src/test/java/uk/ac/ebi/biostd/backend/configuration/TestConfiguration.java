package uk.ac.ebi.biostd.backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biostd.backend.services.RemoteOperations;
import uk.ac.ebi.biostd.backend.services.ResourceHandler;

@Configuration
public class TestConfiguration {

    @Bean
    public RemoteOperations remoteOperationsService(RestTemplate restTemplate) {
        return new RemoteOperations(restTemplate);
    }

    @Bean
    public ResourceHandler resourceHandler() {
        return new ResourceHandler();
    }
}
