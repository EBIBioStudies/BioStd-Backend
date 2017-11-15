/**
 * Copyright 2014-2017 Functional Genomics Development Team, European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * @author Mikhail Gostev <gostev@gmail.com>
 **/

package uk.ac.ebi.biostd.webapp.server.endpoint.attachhost;

import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.ebi.biostd.model.Submission;

@Slf4j
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
