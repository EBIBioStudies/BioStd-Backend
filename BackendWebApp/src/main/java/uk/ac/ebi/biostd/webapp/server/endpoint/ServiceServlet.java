package uk.ac.ebi.biostd.webapp.server.endpoint;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.mng.ServiceConfig;
import uk.ac.ebi.biostd.webapp.server.security.Session;
import uk.ac.ebi.biostd.webapp.server.security.SessionAuthenticated;

public abstract class ServiceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(SC_UNAUTHORIZED);
        response.setContentType("text/plain");
        response.getWriter().print("FAIL User not logged in");
    }

    protected void badRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(SC_BAD_REQUEST);
        response.setContentType("text/plain");
        response.getWriter().print("FAIL Invalid path: " + request.getPathInfo());
    }

    protected void notSuperuser(HttpServletResponse response) throws IOException {
        response.setStatus(SC_BAD_REQUEST);
        response.setContentType("text/plain");
        response.getWriter().print("FAIL only superuser can perform actions on behalf of other user");
    }

    protected boolean isAntonymous(Session session) {
        return session == null || session.isAnonymous();
    }

    protected void service(HttpServletRequest req, HttpServletResponse resp, @AuthenticationPrincipal User user)
            throws ServletException, IOException {
        Path sessionPath = BackendConfig.getWorkDirectory().resolve(ServiceConfig.SessionDir);
        File sessDir = new File(sessionPath.toFile(), "the toekn");
        Session sess = new SessionAuthenticated(sessDir, BackendConfig.getEntityManagerFactory(), user);
        service(req, resp, sess);
    }

    abstract protected void service(HttpServletRequest req, HttpServletResponse resp, Session sess)
            throws ServletException, IOException;
}
