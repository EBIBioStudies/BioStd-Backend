package uk.ac.ebi.biostd.webapp.server.endpoint.submission;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.function.Consumer;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.util.StringUtils;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.endpoint.HttpReqParameterPool;
import uk.ac.ebi.biostd.webapp.server.endpoint.JSONReqParameterPool;
import uk.ac.ebi.biostd.webapp.server.endpoint.ParameterPool;
import uk.ac.ebi.biostd.webapp.server.endpoint.ServiceServlet;
import uk.ac.ebi.biostd.webapp.server.mng.SubmissionManager;
import uk.ac.ebi.biostd.webapp.server.mng.SubmissionSearchRequest;
import uk.ac.ebi.biostd.webapp.server.security.Session;


@WebServlet("/sbmlist")
public class SubmissionListServlet extends ServiceServlet {

    private static final String limitParameter = "limit";
    private static final String offserParameter = "offset";
    private static final String ownerParameter = "owner";
    private static final String ownerIdParameter = "ownerId";
    private static final String accNoParameter = "accNo";
    private static final String keywordsParameter = "keywords";
    private static final String cTimeFromParameter = "cTimeFrom";
    private static final String cTimeToParameter = "cTimeTo";
    private static final String mTimeFromParameter = "mTimeFrom";
    private static final String mTimeToParameter = "mTimeTo";
    private static final String rTimeFromParameter = "rTimeFrom";
    private static final String rTimeToParameter = "rTimeTo";
    private static final String versionFromParameter = "versionFrom";
    private static final String versionToParameter = "versionTo";
    private static final String sortByParameter = "sortBy";

    private static final long serialVersionUID = 1L;

    @Autowired
    private SubmissionManager submissionManager;

    public SubmissionListServlet() {
        super();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp, Session sess)
            throws ServletException, IOException {
        PrintWriter out = resp.getWriter();

        resp.setContentType("application/json; charset=UTF-8");

        if (sess == null || sess.isAnonymous()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\n\"status\": \"FAIL\",\n\"message\": \"User not logged in\"\n}");
            return;
        }

        boolean jsonReq = req.getContentType() != null && req.getContentType().startsWith("application/json");

        ParameterPool params = null;

        if (jsonReq) {
            Charset cs = Charset.defaultCharset();

            String enc = req.getCharacterEncoding();

            if (enc != null) {
                try {
                    cs = Charset.forName(enc);
                } catch (Exception e) {
                }
            }

            String json = StringUtils.readFully(req.getInputStream(), cs);

            if (json.length() == 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\n\"status\": \"FAIL\",\n\"message\": \"Empty JSON request body\"\n}");
                return;
            }

            try {
                params = new JSONReqParameterPool(json, req.getRemoteAddr());
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\n\"status\": \"FAIL\",\n\"message\": \"Empty JSON request body\"\n}");
                return;
            }
        } else {
            params = new HttpReqParameterPool(req);
        }

        int offset = 0;

        String val = params.getParameter(offserParameter);

        if (val != null) {
            try {
                offset = Integer.parseInt(val);
            } catch (Exception e) {
            }
        }

        int limit = -1;

        val = params.getParameter(limitParameter);

        if (val != null) {
            try {
                limit = Integer.parseInt(val);
            } catch (Exception e) {
            }
        }

        SubmissionSearchRequest ssr = new SubmissionSearchRequest();
        boolean ssrSet = false;

        val = params.getParameter(keywordsParameter);

        if (val != null && (val = val.trim()).length() > 0) {
            ssr.setKeywords(val);
            ssrSet = true;
        }

        val = params.getParameter(ownerParameter);

        if (val != null && (val = val.trim()).length() > 0) {
            ssr.setOwner(val);
            ssrSet = true;
        }

        val = params.getParameter(accNoParameter);

        if (val != null && (val = val.trim()).length() > 0) {
            ssr.setAccNo(val);
            ssrSet = true;
        }

        val = params.getParameter(sortByParameter);

        if (val != null && (val = val.trim()).length() > 0) {
            for (SubmissionSearchRequest.SortFields sf : SubmissionSearchRequest.SortFields.values()) {
                if (sf.name().equalsIgnoreCase(val)) {
                    ssr.setSortBy(sf);
                    ssrSet = true;
                    break;
                }
            }
        }

        ssrSet = setIntParameter(params.getParameter(versionFromParameter), ssr::setFromVersion) || ssrSet;
        ssrSet = setIntParameter(params.getParameter(versionToParameter), ssr::setToVersion) || ssrSet;
        ssrSet = setIntParameter(params.getParameter(ownerIdParameter), ssr::setOwnerId) || ssrSet;

        ssrSet = setLongParameter(params.getParameter(cTimeFromParameter), ssr::setFromCTime) || ssrSet;
        ssrSet = setLongParameter(params.getParameter(cTimeToParameter), ssr::setToCTime) || ssrSet;
        ssrSet = setLongParameter(params.getParameter(mTimeFromParameter), ssr::setFromMTime) || ssrSet;
        ssrSet = setLongParameter(params.getParameter(mTimeToParameter), ssr::setToMTime) || ssrSet;
        ssrSet = setLongParameter(params.getParameter(rTimeFromParameter), ssr::setFromRTime) || ssrSet;
        ssrSet = setLongParameter(params.getParameter(rTimeToParameter), ssr::setToRTime) || ssrSet;

        Collection<Submission> subs = null;

        if (ssrSet) {
            ssr.setSkip(offset);
            ssr.setLimit(limit);

            try {
                subs = submissionManager.searchSubmissions(sess.getUser(), ssr);
            } catch (ParseException e) {
                out.print("{\n\"status\": \"FAIL\",\n\"message\": \"Invalid query string\"\n}");
                return;
            }
        } else {
            subs = submissionManager.getSubmissionsByOwner(sess.getUser(), offset, limit);
        }

        out.print("{\n\"status\": \"OK\",\n\"submissions\": [\n");

        if (subs != null) {
            boolean first = true;

            for (Submission s : subs) {
                if (first) {
                    first = false;
                } else {
                    out.print(",\n");
                }

                exportSubmission(s, out);
            }
        }

        out.print("]\n}\n");

    }

    private void exportSubmission(Submission s, Appendable out) throws IOException {
        out.append("{\n\"id\": \"");
        out.append(String.valueOf(s.getId()));
        out.append("\",\n\"accno\": \"");
        StringUtils.appendAsCStr(out, s.getAccNo());
        out.append("\",\n\"title\": \"");
        StringUtils.appendAsJSONStr(out, s.getTitle());
        out.append("\",\n\"ctime\": \"");
        out.append(String.valueOf(s.getCTime()));
        out.append("\",\n\"mtime\": \"");
        out.append(String.valueOf(s.getMTime()));
        out.append("\",\n\"rtime\": \"");
        out.append(String.valueOf(s.getRTime()));
        out.append("\",\n\"version\": \"");
        out.append(String.valueOf(s.getVersion()));
        out.append("\"\n}");

    }

    private boolean setLongParameter(String pval, Consumer<Long> setter) {
        if (pval == null) {
            return false;
        }

        pval = pval.trim();

        try {
            long val = Long.parseLong(pval);

            if (val < 0) {
                val = 0;
            }

            setter.accept(val);
        } catch (Throwable t) {
            return false;
        }

        return true;
    }

    private boolean setIntParameter(String pval, Consumer<Integer> setter) {
        if (pval == null) {
            return false;
        }

        pval = pval.trim();

        try {
            int val = Integer.parseInt(pval);

            setter.accept(val);
        } catch (Throwable t) {
            return false;
        }

        return true;
    }

}
