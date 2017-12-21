package uk.ac.ebi.biostd.webapp.server.endpoint.attachhost;

import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.ebi.biostd.model.Submission;

/**
 * Deprecated in favor of modern implementation of http services see {@link AttachHostListServlet}
 */
@Slf4j
@Deprecated
class JSONRenderer {

    static void render(List<Submission> submissions, Appendable out) throws IOException {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", "OK");
            JSONArray jsonArray = new JSONArray();
            jsonObject.put("submissions", jsonArray);

            for (Submission s : submissions) {
                JSONObject jssub = new JSONObject();

                jssub.put("id", s.getId());
                jssub.put("accno", s.getAccNo());
                jssub.put("title", s.getTitle());
                jssub.put("ctime", s.getCTime());
                jssub.put("mtime", s.getMTime());
                jssub.put("rtime", s.getRTime());
                jssub.put("version", s.getVersion());
                jssub.put("type", s.getRootSection().getType());
                jssub.put("rstitle", Submission.getNodeTitle(s.getRootSection()));

                String val = Submission.getNodeAccNoPattern(s);
                if (val != null) {
                    jssub.put(Submission.canonicAccNoPattern, val);
                }

                val = Submission.getNodeAccNoTemplate(s);
                if (val != null) {
                    jssub.put(Submission.canonicAccNoTemplate, val);
                }

                jsonArray.put(jssub);
            }

            out.append(jsonObject.toString());

        } catch (JSONException exception) {
            log.error("Error while serializing submissions", exception);
        }
    }
}
