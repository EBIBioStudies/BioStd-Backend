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

package uk.ac.ebi.biostd.treelog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.ac.ebi.biostd.in.AccessionMapping;
import uk.ac.ebi.biostd.in.SubmissionMapping;
import uk.ac.ebi.biostd.util.StringUtils;

public class JSON4Mapping {

    public static List<SubmissionMapping> convert(String text) throws ConvertException {
        JSONArray jo = new JSONArray(text);

        return convertJO(jo);
    }

    public static List<SubmissionMapping> convertJO(JSONArray jo) throws ConvertException {
        List<SubmissionMapping> res = new ArrayList<>();

        int len = jo.length();

        for (int i = 0; i < len; i++) {
            JSONObject jsmb = jo.getJSONObject(i);

            SubmissionMapping smap = new SubmissionMapping();

            smap.getSubmissionMapping().setPosition(new int[]{jsmb.getInt("order")});
            smap.getSubmissionMapping().setOrigAcc(jsmb.getString("original"));
            smap.getSubmissionMapping().setAssignedAcc(jsmb.getString("assigned"));

            JSONArray jsecs = jsmb.getJSONArray("sections");

            int slen = jsecs.length();

            for (int j = 0; j < slen; j++) {
                JSONObject jsec = jsecs.getJSONObject(j);

                AccessionMapping secmap = new AccessionMapping();

                secmap.setOrigAcc(jsec.getString("original"));
                secmap.setAssignedAcc(jsec.getString("assigned"));

                JSONArray jord = jsec.getJSONArray("order");

                int olen = jord.length();

                int[] ord = new int[olen];

                for (int k = 0; k < olen; k++) {
                    ord[k] = jord.getInt(k);
                }

                secmap.setPosition(ord);

                smap.addSectionMapping(secmap);
            }

            res.add(smap);
        }

        return res;
    }

    public static void convert(List<SubmissionMapping> smaps, Appendable out) throws IOException {
        out.append("[\n");

        boolean first = true;

        for (SubmissionMapping smap : smaps) {
            if (first) {
                first = false;
            } else {
                out.append(",\n");
            }

            out.append("{\n\"order\": ");
            out.append(String.valueOf(smap.getSubmissionMapping().getPosition()[0]));
            out.append(",\n\"original\": \"");

            if (smap.getSubmissionMapping().getOrigAcc() == null) {
                out.append("\",\n");
            } else {
                StringUtils.appendEscaped(out, smap.getSubmissionMapping().getOrigAcc(), '"', '\\');
                out.append("\",\n");
            }

            out.append("\"assigned\": \"");

            if (smap.getSubmissionMapping().getAssignedAcc() == null) {
                out.append("\",\n");
            } else {
                StringUtils.appendEscaped(out, smap.getSubmissionMapping().getAssignedAcc(), '"', '\\');
                out.append("\",\n");
            }

            out.append("\"sections\": [");

            if (smap.getSectionsMapping() != null) {
                boolean secFirst = true;

                for (AccessionMapping secmap : smap.getSectionsMapping()) {
                    if (secFirst) {
                        secFirst = false;
                    } else {
                        out.append(",\n");
                    }

                    out.append("{\n\"order\": [");

                    boolean pathFirst = true;

                    for (int pEl : secmap.getPosition()) {
                        if (pathFirst) {
                            pathFirst = false;
                        } else {
                            out.append(',');
                        }

                        out.append(String.valueOf(pEl));
                    }

                    out.append("],\n\"original\": \"");

                    if (secmap.getOrigAcc() == null) {
                        out.append("\",\n");
                    } else {
                        StringUtils.appendEscaped(out, secmap.getOrigAcc(), '"', '\\');
                        out.append("\",\n");
                    }

                    out.append("\"assigned\": \"");

                    if (secmap.getAssignedAcc() != null) {
                        StringUtils.appendEscaped(out, secmap.getAssignedAcc(), '"', '\\');
                    }

                    out.append("\"\n}\n");
                }
            }

            out.append("\n]\n}");
        }

        out.append("\n]");

    }
}
