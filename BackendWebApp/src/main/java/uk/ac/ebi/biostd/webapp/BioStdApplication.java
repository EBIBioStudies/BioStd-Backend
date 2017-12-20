package uk.ac.ebi.biostd.webapp;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ServletComponentScan
@ComponentScan(lazyInit = true)
public class BioStdApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        new SpringApplicationBuilder(BioStdApplication.class).run(args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BioStdApplication.class);
    }
}
