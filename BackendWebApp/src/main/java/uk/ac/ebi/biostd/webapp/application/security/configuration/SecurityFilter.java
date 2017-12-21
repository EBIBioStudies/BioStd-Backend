package uk.ac.ebi.biostd.webapp.application.security.configuration;

import com.google.common.base.Strings;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.WebUtils;
import uk.ac.ebi.biostd.authz.Session;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.server.mng.SessionManager;

@AllArgsConstructor
public class SecurityFilter extends GenericFilterBean {

    private static final String COOKIE_NAME = "BIOSTDSESS";
    private SessionManager sessionManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String key = getSecurityKey((HttpServletRequest) request);
        if (Strings.isNullOrEmpty(key)) {
            chain.doFilter(request, response);
            return;
        }

        Session session = sessionManager.checkin(key);
        if (session == null) {
            chain.doFilter(request, response);
            return;
        }

        User user = session.getUser();
        PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(user, null);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private String getSecurityKey(HttpServletRequest httpRequest) {
        Cookie cookie = WebUtils.getCookie(httpRequest, COOKIE_NAME);
        if (cookie != null && !Strings.isNullOrEmpty(cookie.getValue())) {
            return cookie.getValue();
        }

        return httpRequest.getParameter(COOKIE_NAME);
    }
}
