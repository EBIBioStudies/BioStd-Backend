package uk.ac.ebi.biostd.webapp.application.security.rest;

import com.google.common.base.Strings;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.WebUtils;
import uk.ac.ebi.biostd.webapp.application.security.common.ISecurityService;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;

@AllArgsConstructor
public class SecurityFilter extends GenericFilterBean {

    private static final String COOKIE_NAME = "BIOSTDSESS";
    private ISecurityService securityService;

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
        User user = securityService.getUserByKey(key);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getSecurityKey(HttpServletRequest httpRequest) {
        Cookie cookie = WebUtils.getCookie(httpRequest, COOKIE_NAME);
        if (cookie != null && !Strings.isNullOrEmpty(cookie.getValue())) {
            return cookie.getValue();
        }

        return httpRequest.getParameter(COOKIE_NAME);
    }
}
