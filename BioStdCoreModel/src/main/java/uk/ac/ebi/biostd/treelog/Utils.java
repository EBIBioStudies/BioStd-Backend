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
import uk.ac.ebi.biostd.treelog.LogNode.Level;

public class Utils {

    static final Character AI = new Character('\u2502');
    static final Character EL = new Character('\u2514');
    static final Character TEE = new Character('\u251C');

    public static void printLog(LogNode topLn, Appendable out, Level minLevel) throws IOException {

        if (topLn.getLevel().getPriority() < minLevel.getPriority()) {
            return;
        }

        printLog(topLn, out, minLevel, new ArrayList<>());
    }

    private static void printLog(LogNode ln, Appendable out, Level minLevel, List<Character> indent)
            throws IOException {
        for (Character ch : indent) {
            out.append(ch);
        }

        out.append(ln.getLevel().name() + ": " + ln.getMessage()).append('\n');

        int snSz = 0;

        if (ln.getSubNodes() != null) {
            for (LogNode sln : ln.getSubNodes()) {
                if (sln.getLevel().getPriority() >= minLevel.getPriority()) {
                    snSz++;
                }
            }
        }

        if (snSz > 0) {
            if (indent.size() > 0) {
                if (indent.get(indent.size() - 1) == EL) {
                    indent.set(indent.size() - 1, ' ');
                } else if (indent.get(indent.size() - 1) == TEE) {
                    indent.set(indent.size() - 1, AI);
                }
            }

            int n = ln.getSubNodes().size();
            int elgn = 0;

            for (int i = 0; i < n; i++) {
                LogNode snd = ln.getSubNodes().get(i);

                if (snd.getLevel().getPriority() < minLevel.getPriority()) {
                    continue;
                }

                elgn++;

                if (elgn == snSz) {
                    indent.add(EL);
                } else {
                    indent.add(TEE);
                }

                printLog(snd, out, minLevel, indent);

                indent.remove(indent.size() - 1);
            }

        }
    }
}