package uk.ac.ebi.biostd.webapp.application.security.rest;

import com.google.common.base.Strings;
import com.pri.util.collection.Collections;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.WebUtils;
import uk.ac.ebi.biostd.webapp.application.configuration.ConfigProperties;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;
import uk.ac.ebi.biostd.webapp.application.security.service.ISecurityService;
import uk.ac.ebi.biostd.webapp.server.mng.security.SecurityManager;

/**
 * Executed before any request is in charge of obtain and set security user.
 */
public class SecurityFilter extends GenericFilterBean {

    public static final String HEADER_NAME = "X-Session-Token";
    public static final String COOKIE_NAME = "BIOSTDSESS";
    public static final String ENV_VAR = "biostd.environment";

    private final ISecurityService securityService;
    private final SecurityManager securityManager;
    private final String cookieName;

    public SecurityFilter(ISecurityService securityService, SecurityManager securityManager, ConfigProperties config) {
        this.securityService = securityService;
        this.securityManager = securityManager;
        this.cookieName = COOKIE_NAME + "-" + config.get(ENV_VAR);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String key = getSecurityKey((HttpServletRequest) request);
        if (Strings.isNullOrEmpty(key)) {
            chain.doFilter(request, response);
            return;
        }

        authenticateUser(key);
        chain.doFilter(request, response);
    }

    private void authenticateUser(String key) {
        Optional<User> optionalUser = securityService.getUserByKey(key);
        optionalUser.ifPresent(user -> setSecurityUser(user, key));
    }

    private void setSecurityUser(User user, String key) {
        uk.ac.ebi.biostd.authz.User legacyUser = securityManager.getUserById(user.getId());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(legacyUser, key, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getSecurityKey(HttpServletRequest httpRequest) {
        String header = httpRequest.getHeader(HEADER_NAME);
        if (StringUtils.isNotBlank(header)) {
            return header;
        }

        Cookie cookie = WebUtils.getCookie(httpRequest, cookieName);
        if (cookie != null && !Strings.isNullOrEmpty(cookie.getValue())) {
            return cookie.getValue();
        }

        return httpRequest.getParameter(COOKIE_NAME);
    }
}
