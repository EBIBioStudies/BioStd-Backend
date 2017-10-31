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
import org.json.JSONArray;
import org.json.JSONObject;
import uk.ac.ebi.biostd.treelog.LogNode.Level;
import uk.ac.ebi.biostd.util.StringUtils;

public class JSON4Log {

    public static LogNode convert(String text) throws ConvertException {
        JSONObject jo = new JSONObject(text);

        return convertJO(jo);
    }

    public static LogNode convertJO(JSONObject jo) throws ConvertException {
        String lvlStr = jo.getString("level");

        if (lvlStr == null) {
            throw new ConvertException("'level' property missing");
        }

        Level lvl = null;

        try {
            lvl = Level.valueOf(lvlStr);
        } catch (Throwable t) {
        }

        if (lvl == null) {
            throw new ConvertException("Invalid 'level' property value: " + lvlStr);
        }

        String msg = jo.getString("message");

        if (msg == null) {
            throw new ConvertException("'message' property missing");
        }

        SimpleLogNode sln = new SimpleLogNode(lvl, msg, null);

        JSONArray subnodes = jo.optJSONArray("subnodes");

        if (subnodes != null) {
            for (int i = 0; i < subnodes.length(); i++) {
                sln.append(convertJO(subnodes.getJSONObject(i)));
            }
        }

        return sln;
    }

    public static void convert(LogNode nd, Appendable out) throws IOException {
        convertNode(nd, out, 0);
    }

    private static void convertNode(LogNode ln, Appendable sb, int lyr) throws IOException {
        sb.append("{\n");
        sb.append(" \"level\": \"").append(ln.getLevel().name()).append("\",\n");

        boolean needComma = false;

        if (ln.getMessage() != null) {
            sb.append(" \"message\": \"");
            StringUtils.appendAsJSONStr(sb, ln.getMessage());
            sb.append('"');

            needComma = true;
        }

        if (ln.getSubNodes() != null) {
            if (needComma) {
                sb.append(",\n");
            }

            sb.append(" \"subnodes\": [");

            needComma = false;

            for (LogNode snd : ln.getSubNodes()) {
                if (needComma) {
                    sb.append(",\n");
                } else {
                    needComma = true;
                }

                convertNode(snd, sb, lyr + 1);
            }

            sb.append("]\n");
        }

        sb.append("}");
    }
}
