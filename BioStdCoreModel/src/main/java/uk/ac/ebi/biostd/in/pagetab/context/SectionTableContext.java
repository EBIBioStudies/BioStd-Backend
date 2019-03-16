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

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import uk.ac.ebi.biostd.authz.TagRef;
import uk.ac.ebi.biostd.in.CellPointer;
import uk.ac.ebi.biostd.in.pagetab.ParserState;
import uk.ac.ebi.biostd.in.pagetab.SectionOccurrence;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.model.AbstractAttribute;
import uk.ac.ebi.biostd.model.Section;
import uk.ac.ebi.biostd.model.SectionAttribute;
import uk.ac.ebi.biostd.model.SectionAttributeTagRef;
import uk.ac.ebi.biostd.model.trfactory.SectionAttributeTagRefFactory;
import uk.ac.ebi.biostd.model.trfactory.TagReferenceFactory;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.LogNode.Level;

public class SectionTableContext extends TableBlockContext {

    private final String secName;
    private final SectionOccurrence parent;

    private Section current;
    private int tableIdx = -1;

    public SectionTableContext(String sName, SectionOccurrence parentSec, SubmissionInfo si, ParserState prs,
            LogNode sln) {
        super(BlockType.SECTABLE, si, prs, sln);

        secName = sName;
        parent = parentSec;
    }

    @Override
    public AbstractAttribute addAttribute(String nm, String val, Collection<? extends TagRef> tags) {
        SectionAttribute attr = new SectionAttribute();

        attr.setName(nm);
        attr.setValue(val);

        attr.setTagRefs((Collection<SectionAttributeTagRef>) tags);

        attr.setHost(current);
        current.addAttribute(attr);

        return attr;
    }


    @Override
    public TagReferenceFactory<?> getAttributeTagRefFactory() {
        return SectionAttributeTagRefFactory.getInstance();
    }


    @Override
    public void parseLine(List<String> parts, int lineNo) {
        LogNode log = getContextLogNode();

        tableIdx++;
        String acc = parts.get(0).trim();

        if (acc.length() == 0) {
            acc = null;
        }

        current = new Section();

        current.setAccNo(acc);
        current.setType(secName);
        current.setTableIndex(tableIdx);

        super.parseLine(parts, lineNo);

        if (parent != null) {
            parent.getSection().addSection(current);
        }

        if (acc != null) {
            Matcher genAccNoMtch = getParserState().getGeneratedAccNoMatcher();

            genAccNoMtch.reset(acc);

            SectionOccurrence secOc = new SectionOccurrence();

            secOc.setElementPointer(new CellPointer(lineNo, 1));
            secOc.setSection(current);
            secOc.setSecLogNode(log);

            secOc.setOriginalAccNo(acc);

            if (genAccNoMtch.matches()) {
                String pfx = genAccNoMtch.group("pfx");
                String sfx = genAccNoMtch.group("sfx");

                secOc.setPrefix(pfx);
                secOc.setSuffix(sfx);

                current.setAccNo(genAccNoMtch.group("tmpid"));

                if (pfx != null && pfx.length() > 0) {
                    if (Character.isDigit(pfx.charAt(pfx.length() - 1))) {
                        log.log(Level.ERROR,
                                "(R" + lineNo + ",C2) Accession number prefix can't end with a digit '" + pfx + "'");
                    }
                }

                if (sfx != null && sfx.length() > 0) {
                    if (Character.isDigit(sfx.charAt(0))) {
                        log.log(Level.ERROR,
                                "(R" + lineNo + ",C2) Accession number suffix can't start with a digit '" + sfx + "'");
                    }
                }

                getSubmissionInfo().addGlobalSection(secOc);
            } else {
                current.setAccNo(acc);
            }

            if (current.getAccNo() != null) {
                if (getSubmissionInfo().getSectionOccurance(current.getAccNo()) != null) {
                    log.log(Level.ERROR,
                            "Accession number '" + current.getAccNo() + "' is used by other section at " + secOc
                                    .getElementPointer());
                }

                getSubmissionInfo().addSectionOccurance(secOc);
            }

            if (parent != null) {
                secOc.setPosition(parent.incSubSecCount());
                secOc.setParentPath(parent.getPath());
            }
        }

    }


}
