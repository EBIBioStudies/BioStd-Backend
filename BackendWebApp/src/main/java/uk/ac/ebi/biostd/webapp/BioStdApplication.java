package uk.ac.ebi.biostd.webapp;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class BioStdApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(BioStdApplication.class).run(args);
    }
}
