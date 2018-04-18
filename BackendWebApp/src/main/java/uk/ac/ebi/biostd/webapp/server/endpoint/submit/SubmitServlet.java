package uk.ac.ebi.biostd.webapp.server.endpoint.submit;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static uk.ac.ebi.biostd.webapp.server.mng.SubmissionManager.Operation.DELETE;
import static uk.ac.ebi.biostd.webapp.server.mng.SubmissionManager.Operation.REMOVE;
import static uk.ac.ebi.biostd.webapp.server.mng.SubmissionManager.Operation.TRANKLUCATE;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.in.ParserException;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.treelog.JSON4Log;
import uk.ac.ebi.biostd.treelog.JSON4Report;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.SimpleLogNode;
import uk.ac.ebi.biostd.treelog.SubmissionReport;
import uk.ac.ebi.biostd.util.DataFormat;
import uk.ac.ebi.biostd.webapp.server.endpoint.ServiceServlet;
import uk.ac.ebi.biostd.webapp.server.mng.SubmissionManager;
import uk.ac.ebi.biostd.webapp.server.mng.SubmissionManager.Operation;
import uk.ac.ebi.biostd.webapp.server.mng.UserManager;
import uk.ac.ebi.biostd.webapp.server.security.Session;
import uk.ac.ebi.biostd.webapp.server.shared.tags.TagRef;
import uk.ac.ebi.biostd.webapp.server.shared.tags.TagRefParser;

@WebServlet(urlPatterns = "/submit/*")
public class SubmitServlet extends ServiceServlet {

    private static final long serialVersionUID = 1L;

    private static final String VALIDATE_ONLY_PARAMETER = "validateOnly";
    private static final String IGNORE_ABSENT_FILES_PARAMETER = "ignoreAbsentFiles";
    private static final String ID_PARAMETER = "id";
    private static final String ACC_NO_PARAMETER = "accno";
    private static final String ACC_NO_PATTERN_PARAMETER = "accnoPattern";
    private static final String SS_ENABLED_PARAMETER = "sse";
    private static final String TAGS_PARAMETER = "tags";
    private static final String ACCESS_PARAMETER = "access";
    private static final String RELEASE_DATE_PARAMETER = "releaseDate";
    private static final String ON_BEHALF_PARAMETER = "onBehalf";
    private static final String OWNER_PARAMETER = "owner";

    @Autowired
    private UserManager userManager;

    @Autowired
    private SubmissionManager submissionManager;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response, Session sess) throws IOException {

        if (isAntonymous(sess)) {
            unauthorized(response);
            return;
        }

        Operation operation = getOperation(request.getPathInfo());
        if (operation == null) {
            badRequest(request, response);
            return;
        }

        User user = sess.getUser();
        String obUser = request.getParameter(ON_BEHALF_PARAMETER);

        if (obUser != null) {
            if (!user.isSuperuser()) {
                notSuperuser(response);
            }

            user = userManager.getUserByLoginOrEmail(obUser);

            if (user == null && getParameterAsBoolean(request.getParameter(SS_ENABLED_PARAMETER))) {
                user = userManager.addInactiveUser(obUser, request.getParameter("name"));
            }

            if (user == null) {
                response.setStatus(SC_BAD_REQUEST);
                response.setContentType("text/plain");
                response.getWriter().print("FAIL invalid 'onBehalf' user");
            }
        }

        if (operation == DELETE) {
            processDelete(request, response, true, user);
            return;
        }

        if (operation == REMOVE) {
            processDelete(request, response, false, user);
            return;
        }

        if (operation == TRANKLUCATE) {
            processTranklucate(request, response, user);
            return;
        }

        if (operation == Operation.SETMETA) {
            processSetMeta(request, response, user);
            return;
        }

        if (operation == Operation.CHOWN) {
            processChown(request, response, user);
            return;
        }

        String cType = request.getContentType();

        if (cType == null) {
            response.setStatus(SC_BAD_REQUEST);
            response.getWriter().print("FAIL 'Content-type' header missing");
            return;
        }

        int pos = cType.indexOf(';');

        if (pos > 0) {
            cType = cType.substring(0, pos).trim();
        }

        DataFormat fmt = null;

        for (DataFormat f : DataFormat.values()) {
            if (f.getContentType().equalsIgnoreCase(cType)) {
                fmt = f;
                break;
            }
        }

        if (fmt == null) {
            response.setStatus(SC_BAD_REQUEST);
            response.getWriter().print("FAIL Content type '" + cType + "' is not supported");
            return;
        }

        byte[] data = IOUtils.toByteArray(request.getInputStream());

        if (data.length == 0) {
            response.setStatus(SC_BAD_REQUEST);
            response.getWriter().print("FAIL Empty request body");
            return;
        }

        boolean validateOnly = getParameterAsBoolean(request.getParameter(VALIDATE_ONLY_PARAMETER));
        boolean ignAbsFiles = getParameterAsBoolean(request.getParameter(IGNORE_ABSENT_FILES_PARAMETER));

        SubmissionReport submissionReport = submissionManager.createSubmission(
                data, fmt, request.getCharacterEncoding(), operation, user, validateOnly, ignAbsFiles);

        LogNode topLn = submissionReport.getLog();
        SimpleLogNode.setLevels(topLn);
        response.setContentType("application/json");
        JSON4Report.convert(submissionReport, response.getWriter());
    }

    private boolean getParameterAsBoolean(String parameter) {
        return parameter != null &&
                ("true".equalsIgnoreCase(parameter) || "yes".equalsIgnoreCase(parameter) || "1".equals(parameter));
    }

    private Operation getOperation(String pathInfo) {
        Operation operation = null;

        if (pathInfo != null && pathInfo.length() > 1) {
            pathInfo = pathInfo.substring(1);

            for (Operation op : Operation.values()) {
                if (op.name().equalsIgnoreCase(pathInfo)) {
                    operation = op;
                    break;
                }
            }
        }

        return operation;
    }

    private void processSetMeta(HttpServletRequest request, HttpServletResponse response, User usr) throws IOException {
        String sbmAcc = request.getParameter(ACC_NO_PARAMETER);

        if (sbmAcc == null) {
            sbmAcc = request.getParameter(ID_PARAMETER);
        }

        if (sbmAcc == null) {
            response.setStatus(SC_BAD_REQUEST);
            response.getWriter().print("FAIL '" + ACC_NO_PARAMETER + "' parameter is not specified");
            return;
        }

        String val = request.getParameter(TAGS_PARAMETER);

        List<TagRef> tags = null;

        if (val != null) {
            try {
                tags = TagRefParser.parseTags(val);
            } catch (ParserException e) {
                response.setStatus(SC_BAD_REQUEST);
                response.getWriter().print("FAIL invalid '" + TAGS_PARAMETER + "' parameter value");
                return;
            }
        }

        val = request.getParameter(ACCESS_PARAMETER);

        Set<String> access = null;

        if (val != null) {
            val = val.trim();

            if (val.length() == 0) {
                access = Collections.emptySet();
            } else {
                access = new HashSet<>();

                for (String s : val.split(",")) {
                    access.add(s.trim());
                }
            }

        }

        val = request.getParameter(RELEASE_DATE_PARAMETER);

        long rTime = -1;

        if (val != null) {
            rTime = Submission.readReleaseDate(val);

            if (rTime < 0) {
                response.setStatus(SC_BAD_REQUEST);
                response.getWriter().print("FAIL Invalid '" + RELEASE_DATE_PARAMETER
                        + "' parameter value. Expected date in format: YYYY-MM-DD[Thh:mm[:ss[.mmm]]]");
                return;
            }

        }

        LogNode topLn = submissionManager.updateSubmissionMeta(sbmAcc, tags, access, rTime, usr);
        SimpleLogNode.setLevels(topLn);
        JSON4Log.convert(topLn, response.getWriter());

    }

    private void processDelete(HttpServletRequest request, HttpServletResponse response, boolean toHistory, User usr)
            throws IOException {
        String sbmAcc = request.getParameter(ACC_NO_PARAMETER);

        if (sbmAcc == null) {
            sbmAcc = request.getParameter("id");
        }

        if (sbmAcc == null) {
            response.setStatus(SC_BAD_REQUEST);
            response.getWriter().print("FAIL 'id' parameter is not specified");
            return;
        }

        response.setContentType("application/json");
        LogNode topLn = submissionManager.deleteSubmissionByAccession(sbmAcc, toHistory, usr);
        SimpleLogNode.setLevels(topLn);
        JSON4Log.convert(topLn, response.getWriter());

    }

    private void processChown(HttpServletRequest request, HttpServletResponse response, User usr) throws IOException {
        String sbmAcc = request.getParameter(ACC_NO_PARAMETER);
        String patAcc = request.getParameter(ACC_NO_PATTERN_PARAMETER);

        if (patAcc == null) {
            if (sbmAcc == null) {
                response.setStatus(SC_BAD_REQUEST);
                response.getWriter().print("FAIL '" + ACC_NO_PARAMETER + "' or '" + ACC_NO_PATTERN_PARAMETER
                        + "' parameter is not specified");
                return;
            }
        } else if (sbmAcc != null) {
            response.setStatus(SC_BAD_REQUEST);
            response.getWriter().print("FAIL Parameters '" + ACC_NO_PARAMETER + "' and '" + ACC_NO_PATTERN_PARAMETER
                    + "' can'n be used at the same time");
            return;
        }

        if (patAcc != null) {
            if (patAcc.length() < 5) {
                response.setStatus(SC_BAD_REQUEST);
                response.getWriter()
                        .print("FAIL Invalid '" + ACC_NO_PATTERN_PARAMETER + "' parameter value. Pattern is too short");
                return;
            }

            int pfxLen = 0;

            for (int i = 0; i < patAcc.length(); i++) {
                char c = patAcc.charAt(i);

                if (c == '?' || c == '%') {
                    break;
                }

                pfxLen++;
            }

            if (pfxLen < 5) {
                response.setStatus(SC_BAD_REQUEST);
                response.getWriter().print("FAIL Invalid '" + ACC_NO_PATTERN_PARAMETER
                        + "' parameter value. Pattern is too loose. Should have 5 characters prefix");
                return;
            }
        }

        String owner = request.getParameter(OWNER_PARAMETER);

        if (owner == null) {
            response.setStatus(SC_BAD_REQUEST);
            response.getWriter().print("FAIL '" + OWNER_PARAMETER + "' parameter missing");
            return;
        }

        LogNode topLn = sbmAcc != null ?
                submissionManager.changeOwnerByAccession(sbmAcc, owner, usr) :
                submissionManager.changeOwnerByAccessionPattern(patAcc, owner, usr);

        SimpleLogNode.setLevels(topLn);
        response.setContentType("application/json");
        JSON4Log.convert(topLn, response.getWriter());
    }

    private void processTranklucate(HttpServletRequest request, HttpServletResponse response, User usr)
            throws IOException {
        String sbmID = request.getParameter(ID_PARAMETER);
        String sbmAcc = request.getParameter(ACC_NO_PARAMETER);
        String patAcc = request.getParameter(ACC_NO_PATTERN_PARAMETER);

        boolean clash = false;

        if (patAcc != null) {
            if (sbmAcc != null || sbmID != null) {
                clash = true;
            }
        } else if (sbmAcc != null) {
            if (patAcc != null || sbmID != null) {
                clash = true;
            }
        } else if (sbmID != null) {
            if (patAcc != null || sbmAcc != null) {
                clash = true;
            }
        } else {
            response.setStatus(SC_BAD_REQUEST);
            response.getWriter()
                    .print("FAIL '" + ID_PARAMETER + "' or '" + ACC_NO_PARAMETER + "' or '" + ACC_NO_PATTERN_PARAMETER
                            + "' parameter is not specified");
            return;
        }

        if (clash) {
            response.setStatus(SC_BAD_REQUEST);
            response.getWriter().print("FAIL Parameters '" + ID_PARAMETER + "', '" + ACC_NO_PARAMETER + "' and '"
                    + ACC_NO_PATTERN_PARAMETER + "' can'n be used at the same time");
            return;
        }

        int id = -1;

        if (sbmID != null) {
            try {
                id = Integer.parseInt(sbmID);
            } catch (Exception e) {
                response.setStatus(SC_BAD_REQUEST);
                response.getWriter().print("FAIL Invalid '" + ID_PARAMETER + "' parameter value. Must be integer");
                return;
            }
        }

        if (patAcc != null) {
            if (patAcc.length() < 5) {
                response.setStatus(SC_BAD_REQUEST);
                response.getWriter()
                        .print("FAIL Invalid '" + ACC_NO_PATTERN_PARAMETER + "' parameter value. Pattern is too short");
                return;
            }

            int pfxLen = 0;

            for (int i = 0; i < patAcc.length(); i++) {
                char c = patAcc.charAt(i);

                if (c == '?' || c == '%') {
                    break;
                }

                pfxLen++;
            }

            if (pfxLen < 5) {
                response.setStatus(SC_BAD_REQUEST);
                response.getWriter().print("FAIL Invalid '" + ACC_NO_PATTERN_PARAMETER
                        + "' parameter value. Pattern is too loose. Should have 5 characters prefix");
                return;
            }
        }

        response.setContentType("application/json");
        LogNode topLn = null;

        if (sbmID != null) {
            topLn = submissionManager.tranklucateSubmissionById(id, usr);
        } else if (sbmAcc != null) {
            topLn = submissionManager.tranklucateSubmissionByAccession(sbmAcc, usr);
        } else {
            topLn = submissionManager.tranklucateSubmissionByAccessionPattern(patAcc, usr);
        }

        SimpleLogNode.setLevels(topLn);
        JSON4Log.convert(topLn, response.getWriter());
    }
}
