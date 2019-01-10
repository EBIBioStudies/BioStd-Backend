package uk.ac.ebi.biostd.webapp.application.configuration;

import static uk.ac.ebi.biostd.commons.files.MagicFolderUtil.USER_GROUP_DIR_PROP_NAME;
import static uk.ac.ebi.biostd.webapp.server.config.ConfigurationManager.BIOSTUDY_BASE_DIR;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.biostd.commons.files.MagicFolderUtil;

@Configuration
public class GeneralConfig {

    private static final String USER_SYMLINK_PATH = "/usergroup/Users";

    @Bean
    public MagicFolderUtil magicFolderUtil(ConfigProperties configProperties) {
        return new MagicFolderUtil(
                configProperties.get(USER_GROUP_DIR_PROP_NAME),
                configProperties.get(BIOSTUDY_BASE_DIR) + USER_SYMLINK_PATH);
    }
}
