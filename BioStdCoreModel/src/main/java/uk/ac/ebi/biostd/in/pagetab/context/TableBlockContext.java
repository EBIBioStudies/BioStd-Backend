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

package uk.ac.ebi.biostd.in.pagetab.context;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import uk.ac.ebi.biostd.in.CellPointer;
import uk.ac.ebi.biostd.in.pagetab.ParserState;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.model.AbstractAttribute;
import uk.ac.ebi.biostd.model.Qualifier;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.LogNode.Level;

public abstract class TableBlockContext extends BlockContext {


    private final Matcher nameQualifierMatcher;
    private final Matcher valueQualifierMatcher;
    private final Matcher referenceMatcher;
    private final SubmissionInfo submInfo;
    private List<AttrRef> atRefs;

    protected TableBlockContext(BlockType typ, SubmissionInfo si, ParserState ps, LogNode ln) {
        super(typ, ps, ln);

        nameQualifierMatcher = ps.getNameQualifierMatcher();
        valueQualifierMatcher = ps.getValueQualifierMatcher();
        referenceMatcher = ps.getReferenceMatcher();

        submInfo = si;
    }

    public SubmissionInfo getSubmissionInfo() {
        return submInfo;
    }

    @Override
    public void parseFirstLine(List<String> parts, int lineNo) {
        LogNode log = getContextLogNode();

        atRefs = new ArrayList<>(parts.size());

        int emptyIdx = -1;

        boolean hasAttr = false;

        for (int i = 1; i < parts.size(); i++) {
            String nm = parts.get(i).trim();

            if (nm.length() == 0) {
                emptyIdx = i + 1;

                atRefs.add(new AttrRef());

                continue;
            }

            if (emptyIdx > 0) {
                log.log(Level.ERROR, "(R" + lineNo + ",C" + emptyIdx + ") Missed attribute name");
                emptyIdx = -1;
            }

            nameQualifierMatcher.reset(nm);

            if (nameQualifierMatcher.matches()) {
                log.log(Level.ERROR,
                        "(R" + lineNo + ",C" + (i + 1) + ") Attribute name qualifier is not allowed in table blocks");
                atRefs.add(new AttrRef());
                continue;
            }

            valueQualifierMatcher.reset(nm);

            AttrRef atr = new AttrRef();

            if (valueQualifierMatcher.matches()) {
                if (!hasAttr) {
                    log.log(Level.ERROR, "(R" + lineNo + ",C" + (i + 1) + ") Qualifier must follow an attribute");
                    nm = null;
                } else {
                    nm = valueQualifierMatcher.group("name").trim();
                }

                atr.classifier = true;
            } else {
                referenceMatcher.reset(nm);

                if (referenceMatcher.matches()) {
                    nm = referenceMatcher.group("name").trim();
                    atr.reference = true;
                }

                hasAttr = true;
            }

            atr.name = nm;
            atRefs.add(atr);
        }

    }

    @Override
    public void parseLine(List<String> parts, int lineNo) {
        LogNode log = getContextLogNode();

        int nvals = parts.size() - 1;

        int n = atRefs.size() >= nvals ? atRefs.size() : nvals;

        AbstractAttribute prevAttr = null;

        for (int i = 0; i < n; i++) {
            String val = null;

            if (i < nvals) {
                val = parts.get(i + 1).trim();
            }

            if (i >= atRefs.size()) {
                if (val.length() != 0) {
                    log.log(Level.ERROR, "(R" + lineNo + ",C" + (i + 1) + ") Unexpected value");
                }

                continue;
            }

            AttrRef atr = atRefs.get(i);

            if (atr.name == null) {
                continue;
            }

            if (val == null || val.length() == 0) {
                continue;
            }

            if (atr.classifier) {
                prevAttr.addValueQualifier(new Qualifier(atr.name, val));
            } else if (atr.reference) {
                prevAttr = addAttribute(atr.name, val, null);
                prevAttr.setReference(true);
                submInfo.addReferenceOccurance(new CellPointer(lineNo, i + 1), prevAttr, log);
            } else {
                prevAttr = addAttribute(atr.name, val, null);
            }
        }

//  for( int i = atRefs.size()+1; i < parts.size(); i++ )
//  {
//   if( parts.get(i).trim().length() != 0 )
//    log.log(Level.ERROR, "(R" + lineNo + ",C"+(i+1)+") Unexpected value");
//  }

    }

    static class AttrRef {

        String name;
        boolean classifier = false;
        boolean reference = false;
    }

}
