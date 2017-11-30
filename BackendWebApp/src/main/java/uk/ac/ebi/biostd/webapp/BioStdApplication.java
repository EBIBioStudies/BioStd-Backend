package uk.ac.ebi.biostd.webapp;

import javax.servlet.ServletContextListener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import uk.ac.ebi.biostd.webapp.server.WebAppInit;
import uk.ac.ebi.biostd.webapp.server.config.ConfigurationManager;

@SpringBootApplication
@ServletComponentScan
public class BioStdApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        new SpringApplicationBuilder(BioStdApplication.class).run(args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BioStdApplication.class);
    }

    @Bean
    ServletListenerRegistrationBean<ServletContextListener> myServletListener(ConfigurationManager configManager) {
        ServletListenerRegistrationBean<ServletContextListener> srb = new ServletListenerRegistrationBean<>();
        srb.setListener(new WebAppInit(configManager));
        return srb;
    }
}
