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
import java.util.Collection;
import java.util.List;
import uk.ac.ebi.biostd.authz.TagRef;
import uk.ac.ebi.biostd.in.pagetab.ParserState;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.model.AbstractAttribute;
import uk.ac.ebi.biostd.model.Section;
import uk.ac.ebi.biostd.model.SectionAttribute;
import uk.ac.ebi.biostd.model.SectionAttributeTagRef;
import uk.ac.ebi.biostd.model.trfactory.SectionAttributeTagRefFactory;
import uk.ac.ebi.biostd.model.trfactory.SectionTagRefFactory;
import uk.ac.ebi.biostd.model.trfactory.TagReferenceFactory;
import uk.ac.ebi.biostd.treelog.LogNode;

public class SectionContext extends VerticalBlockContext {

    private final Section section;
    private final List<SectionContext> sections = new ArrayList<>();

    public SectionContext(Section sec, SubmissionInfo si, ParserState prs, LogNode sln) {
        super(BlockType.SECTION, si, prs, sln);

        section = sec;
    }

    public Section getSection() {
        return section;
    }

    public void addSubSection(SectionContext sc) {
        sections.add(sc);
    }

    public List<SectionContext> getSubSections() {
        return sections;
    }

    @Override
    public AbstractAttribute addAttribute(String nm, String val, Collection<? extends TagRef> tags) {
        SectionAttribute attr = new SectionAttribute();

        attr.setName(nm);
        attr.setValue(val);

        attr.setTagRefs((Collection<SectionAttributeTagRef>) tags);

        attr.setHost(section);
        section.addAttribute(attr);

        return attr;
    }


    @Override
    public TagReferenceFactory<?> getAttributeTagRefFactory() {
        return SectionAttributeTagRefFactory.getInstance();
    }

    @Override
    public void parseFirstLine(List<String> cells, int ln) {
        LogNode log = getContextLogNode();

        String acc = null;

        if (cells.size() > 1) {
            acc = cells.get(1).trim();
        }

        if (acc != null && acc.length() > 0) {
            section.setAccNo(acc);
        }

        acc = null;

        if (cells.size() > 2) {
            acc = cells.get(2).trim();
        }

        if (acc != null && acc.length() > 0) {
            section.setParentAccNo(acc);
        }

        section.setAccessTags(getParserState().getParser().processAccessTags(cells, ln, 4, log));
        section.setTagRefs(
                getParserState().getParser().processTags(cells, ln, 5, SectionTagRefFactory.getInstance(), log));
    }

}
