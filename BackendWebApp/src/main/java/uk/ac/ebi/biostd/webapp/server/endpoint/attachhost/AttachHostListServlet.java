package uk.ac.ebi.biostd.webapp.server.endpoint.attachhost;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.webapp.application.security.rest.SecurityController;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.endpoint.JSONHttpResponse;
import uk.ac.ebi.biostd.webapp.server.endpoint.Response;
import uk.ac.ebi.biostd.webapp.server.endpoint.ServiceServlet;
import uk.ac.ebi.biostd.webapp.server.endpoint.TextHttpResponse;
import uk.ac.ebi.biostd.webapp.server.security.Session;

/**
 * @WebServlet("/atthost") deprecated in favor of use {@link SecurityController#getProjects(User)} which use modern
 * approach to rest service implementation.
 */
@Deprecated
public class AttachHostListServlet extends ServiceServlet {

    private static final String FORMAT_PARAMETER = "format";
    private static final String TYPE_PARAMETER = "type";
    private static final String DEFAULT_RESPONSE_FORMAT = "xml";
    private static final String JSON_FORMAT = "json";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp, Session session)
            throws ServletException, IOException {

        PrintWriter out = resp.getWriter();
        String format = req.getParameter(FORMAT_PARAMETER);

        if (format == null) {
            format = DEFAULT_RESPONSE_FORMAT;
        }

        if (session == null || session.isAnonymous()) {
            getResponse(format, resp).respond(SC_UNAUTHORIZED, "FAIL", "User not logged in");
            return;
        }

        String type = req.getParameter(TYPE_PARAMETER);

        if (type == null || type.length() < 1) {
            getResponse(format, resp).respond(SC_BAD_REQUEST, "FAIL", "Invalid request. Type is missing");
            return;
        }

        List<Submission> subs = BackendConfig.getServiceManager().getSubmissionManager()
                .getHostSubmissionsByType(type, session.getUser());

        if ("json".equalsIgnoreCase(format)) {
            JSONRenderer.render(subs, out);
        } else {
            subs.forEach(s -> out.printf("ID:%d AccNo:%s Title: %s\n", s.getId(), s.getAccNo(), s.getTitle()));
        }
    }

    private Response getResponse(String fmt, HttpServletResponse response) {
        return JSON_FORMAT.equalsIgnoreCase(fmt) ? new JSONHttpResponse(response) : new TextHttpResponse(response);
    }
}
