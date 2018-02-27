package uk.ac.ebi.biostd.webapp.server.endpoint.submit;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;
import uk.ac.ebi.biostd.treelog.JSON4Report;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.SimpleLogNode;
import uk.ac.ebi.biostd.treelog.SubmissionReport;
import uk.ac.ebi.biostd.util.DataFormat;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.endpoint.ServiceServlet;
import uk.ac.ebi.biostd.webapp.server.mng.SubmissionManager.Operation;
import uk.ac.ebi.biostd.webapp.server.security.Session;

@MultipartConfig
@WebServlet(urlPatterns = "/formsubmit")
public class FormSubmitServlet extends ServiceServlet {

    private static final String convertOperation = "convert";
    private static final String typeParameter = "type";
    private static final String validateOnlyParameter = "validateOnly";
    private static final String ignoreAbsentFilesParameter = "ignoreAbsentFiles";
    private static final String opParameter = "op";
    private static final String outFormat = "outFormat";
    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse response, Session sess)
            throws ServletException, IOException {
        if (!req.getMethod().equalsIgnoreCase("POST")) {
            respond(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "Method " + req.getMethod() + " is not allowed");
            return;
        }

        if (sess == null || sess.isAnonymous()) {
            respond(response, HttpServletResponse.SC_UNAUTHORIZED, "FAIL User not logged in");
            return;
        }

        Part filePart = null;

        if (req.getParts().size() < 1) {
            respond(response, HttpServletResponse.SC_BAD_REQUEST, "FAIL Invalid number of file parts");
            return;
        }

        filePart = req.getPart("file");

        String fmtStr = req.getParameter(typeParameter);

        DataFormat fmt = null;

        if (fmtStr != null && fmtStr.trim().length() > 0) {

            try {
                fmt = DataFormat.valueOf(DataFormat.class, fmtStr);
            } catch (Exception e) {
                respond(response, HttpServletResponse.SC_BAD_REQUEST,
                        "FAIL Invalid 'type' attribute value: '" + fmtStr + "'");
                return;
            }
        }

        if (fmt == null) {
            String fn = filePart.getSubmittedFileName();

            if (fn != null) {
                int pos = fn.lastIndexOf('.');

                if (pos > 0) {
                    try {
                        fmt = DataFormat.valueOf(DataFormat.class, fn.substring(pos + 1).toLowerCase());
                    } catch (Exception e) {
                    }
                }
            }
        }

        if (fmt == null) {
            respond(response, HttpServletResponse.SC_BAD_REQUEST,
                    "FAIL can't determine file type. Define 'type' attribute");
            return;
        }

        Operation act = null;

        String pi = req.getParameter(opParameter);

        if (pi != null && pi.length() > 1) {
            for (Operation op : Operation.values()) {
                if (op.name().equalsIgnoreCase(pi)) {
                    act = op;
                    break;
                }
            }
        }

        if (act == null && convertOperation.equalsIgnoreCase(pi)) {
            Converter.convert(IOUtils.toByteArray(filePart.getInputStream()), fmt, req.getParameter(outFormat),
                    response);
            return;
        }
        if (Operation.DELETE == act || Operation.CHOWN == act || Operation.REMOVE == act || Operation.SETMETA == act
                || Operation.TRANKLUCATE == act) {
            act = null;
        }

        if (act == null) {
            respond(response, HttpServletResponse.SC_BAD_REQUEST, "FAIL operation invalid or not defined");
            return;
        }

        byte[] data = IOUtils.toByteArray(filePart.getInputStream());

        if (data.length == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("FAIL Empty file");
            return;
        }

        String vldPrm = req.getParameter(validateOnlyParameter);
        String ignPrm = req.getParameter(ignoreAbsentFilesParameter);

        boolean validateOnly =
                vldPrm != null && ("true".equalsIgnoreCase(vldPrm) || "yes".equalsIgnoreCase(vldPrm) || "on"
                        .equalsIgnoreCase(vldPrm) || "1".equals(vldPrm));
        boolean ignAbsFiles =
                ignPrm != null && ("true".equalsIgnoreCase(ignPrm) || "yes".equalsIgnoreCase(ignPrm) || "on"
                        .equalsIgnoreCase(ignPrm) || "1".equals(ignPrm));

        SubmissionReport res = BackendConfig.getServiceManager().getSubmissionManager()
                .createSubmission(data, fmt, "UTF-8", act, sess.getUser(), validateOnly, ignAbsFiles);

        LogNode topLn = res.getLog();

        SimpleLogNode.setLevels(topLn);

        response.setContentType("application/json");

        JSON4Report.convert(res, response.getWriter());
    }

    private void respond(HttpServletResponse response, int code, String msg) throws IOException {
        response.setStatus(code);
        response.setContentType("text/plain");
        response.getWriter().print(msg);
    }

}
