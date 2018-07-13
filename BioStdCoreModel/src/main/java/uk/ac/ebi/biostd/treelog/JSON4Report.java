/**
 * Copyright 2014-2017 Functional Genomics Development Team, European Bioinformatics Institute
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * @author Mikhail Gostev <gostev@gmail.com>
 **/

package uk.ac.ebi.biostd.treelog;

import java.io.IOException;
import org.json.JSONObject;
import uk.ac.ebi.biostd.treelog.LogNode.Level;

public class JSON4Report {

    public static SubmissionReport convert(String text) throws ConvertException {
        JSONObject jo = new JSONObject(text);

        return convertJO(jo);

    }

    public static SubmissionReport convertJO(JSONObject jo) throws ConvertException {
        SubmissionReport rep = new SubmissionReport();

        rep.setLog(JSON4Log.convertJO(jo.getJSONObject("log")));

        rep.setSubmissionMappings(JSON4Mapping.convertJO(jo.getJSONArray("mapping")));

        return rep;
    }

    public static void convert(SubmissionReport rep, Appendable out) throws IOException {
        out.append("{\n\"status\": \"");

        out.append(rep.getStatus());

        out.append("\",\n\"mapping\": ");

        JSON4Mapping.convert(rep.getMappings(), out);

        out.append(",\n\"log\": ");

        JSON4Log.convert(rep.getLog(), out);

        out.append("}");
    }
}
