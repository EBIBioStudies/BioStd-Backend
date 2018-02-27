package uk.ac.ebi.biostd.webapp.application.security.configuration;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.security.Http401AuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import uk.ac.ebi.biostd.webapp.application.security.common.ISecurityService;
import uk.ac.ebi.biostd.webapp.application.security.rest.SecurityFilter;
import uk.ac.ebi.biostd.webapp.server.mng.security.SecurityManager;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final ISecurityService securityService;
    private final SecurityManager securityManager;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .addFilterAfter(new SecurityFilter(securityService, securityManager), BasicAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/**").permitAll()
                .and()
                .exceptionHandling().authenticationEntryPoint(http401AuthenticationEntryPoint());
    }

    @Bean
    public Http401AuthenticationEntryPoint http401AuthenticationEntryPoint() {
        return new Http401AuthenticationEntryPoint("Bearer realm=\"webrealm\"");
    }
}
