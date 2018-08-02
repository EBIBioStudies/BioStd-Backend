package uk.ac.ebi.biostd.webapp.application.security.configuration;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import uk.ac.ebi.biostd.webapp.application.configuration.ConfigProperties;
import uk.ac.ebi.biostd.webapp.application.security.rest.SecurityFilter;
import uk.ac.ebi.biostd.webapp.application.security.service.ISecurityService;
import uk.ac.ebi.biostd.webapp.server.mng.security.SecurityManager;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] ALLOWED_URLS = {
            "/auth/signin",
            "/auth/passreset",
            "/auth/passrstre",
            "/auth/signup",
            "/auth/activate/*",
            "/auth/passrstreq",
            "/auth/passreset",
            "/checkAccess",
            "/test/**"
    };

    private final ISecurityService securityService;
    private final SecurityManager securityManager;
    private final ConfigProperties config;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .addFilterBefore(
                        new SecurityFilter(securityService, securityManager, config),BasicAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(ALLOWED_URLS).permitAll()
                .anyRequest().fullyAuthenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
    }

    @Bean
    public static JwtParser jwtParser() {
        return Jwts.parser();
    }
}
