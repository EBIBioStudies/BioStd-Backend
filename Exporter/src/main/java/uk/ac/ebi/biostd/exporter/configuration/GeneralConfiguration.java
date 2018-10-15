package uk.ac.ebi.biostd.exporter.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biostd.commons.files.MagicFolderUtil;
import uk.ac.ebi.biostd.exporter.jobs.users.UsersFoldersProperties;
import uk.ac.ebi.biostd.remote.service.RemoteService;

/**
 * Contains Beans declarations required as dependencies al around the project.
 */
@Configuration
@Slf4j
public class GeneralConfiguration {
    @Value("{jobs.libFileProjects}")
    private List<String> libFileProjects;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        return mapper;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RemoteService remoteService(@Value("${jobs.backend-url}") String backendUrl) {
        log.info("creating remote service with url {}", backendUrl);
        return new RemoteService(backendUrl);
    }

    @Bean
    public MagicFolderUtil magicFolderUtil(UsersFoldersProperties properties) {
        return new MagicFolderUtil(properties.getBaseDropboxPath(), properties.getSymLinksPath());
    }

    @Bean
    public List<String> libFileProjects() {
        return libFileProjects;
    }
}
