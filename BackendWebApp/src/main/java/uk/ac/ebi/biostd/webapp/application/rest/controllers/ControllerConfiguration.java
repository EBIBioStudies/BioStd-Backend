package uk.ac.ebi.biostd.webapp.application.rest.controllers;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class ControllerConfiguration extends WebMvcConfigurerAdapter {
    @Override
    public void configurePathMatch(PathMatchConfigurer pathMatchConfigurer) {
        AntPathMatcher matcher = new AntPathMatcher();
        matcher.setCaseSensitive(false);
        pathMatchConfigurer.setPathMatcher(matcher);
    }
}
