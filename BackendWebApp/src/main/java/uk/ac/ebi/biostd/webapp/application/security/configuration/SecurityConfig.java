package uk.ac.ebi.biostd.webapp.application.security.configuration;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.security.Http401AuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import uk.ac.ebi.biostd.webapp.application.security.rest.SecurityFilter;
import uk.ac.ebi.biostd.webapp.application.security.service.ISecurityService;
import uk.ac.ebi.biostd.webapp.server.mng.security.SecurityManager;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] ALLOWED_URLS = {
            "/auth/signin",
            "/auth/passreset",
            "/auth/passrstre",
            "/auth/signup",
            "/auth/activate/*",
            "/auth/passrstreq",
            "/auth/passreset"
    };

    private final ISecurityService securityService;
    private final SecurityManager securityManager;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .addFilterBefore(new SecurityFilter(securityService, securityManager), BasicAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(ALLOWED_URLS).permitAll()
                .anyRequest().fullyAuthenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(http401AuthenticationEntryPoint());

        http.addFilterBefore(new SecurityFilter(securityService, securityManager), BasicAuthenticationFilter.class);
    }

    @Bean
    public Http401AuthenticationEntryPoint http401AuthenticationEntryPoint() {
        return new Http401AuthenticationEntryPoint("Bearer realm=\"webrealm\"");
    }
}
