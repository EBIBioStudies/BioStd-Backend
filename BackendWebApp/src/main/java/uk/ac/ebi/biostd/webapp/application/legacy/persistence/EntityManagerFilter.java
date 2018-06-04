package uk.ac.ebi.biostd.webapp.application.legacy.persistence;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;

@Component
public class EntityManagerFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(req, res);
        } finally {
            BackendConfig.getServiceManager().closeEntityManager();
        }
    }
}
