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
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.model.SubmissionAttribute;
import uk.ac.ebi.biostd.model.SubmissionAttributeTagRef;
import uk.ac.ebi.biostd.model.trfactory.SubmissionAttributeTagRefFactory;
import uk.ac.ebi.biostd.model.trfactory.SubmissionTagRefFactory;
import uk.ac.ebi.biostd.model.trfactory.TagReferenceFactory;
import uk.ac.ebi.biostd.treelog.LogNode;

public class SubmissionContext extends VerticalBlockContext {


    private final List<SectionContext> sections = new ArrayList<>();

    public SubmissionContext(SubmissionInfo si, ParserState pars, LogNode sln /*, BlockContext pc */) {
        super(BlockType.SUBMISSION, si, pars, sln);
    }

    public void addSection(SectionContext sc) {
        sections.add(sc);
    }

    public List<SectionContext> getSections() {
        return sections;
    }

    @Override
    public void parseFirstLine(List<String> cells, int ln) {
        Submission submission = getSubmissionInfo().getSubmission();

        LogNode log = getContextLogNode();

        String acc = null;

        if (cells.size() > 1) {
            acc = cells.get(1).trim();
        }

        if (acc != null && acc.length() > 0) {
            submission.setAccNo(acc);
        }
//   else
//    log.log(Level.ERROR, "(R"+ln+",C2) Missing submission ID");

        submission.setAccessTags(getParserState().getParser().processAccessTags(cells, ln, 3, log));
        submission.setTagRefs(
                getParserState().getParser().processTags(cells, ln, 4, SubmissionTagRefFactory.getInstance(), log));

    }


    @Override
    public AbstractAttribute addAttribute(String nm, String val, Collection<? extends TagRef> tags) {
        Submission submission = getSubmissionInfo().getSubmission();

        SubmissionAttribute attr = new SubmissionAttribute();

        attr.setName(nm);
        attr.setValue(val);

        attr.setTagRefs((Collection<SubmissionAttributeTagRef>) tags);

        attr.setHost(submission);
        submission.addAttribute(attr);

        return attr;
    }

    @Override
    public TagReferenceFactory<?> getAttributeTagRefFactory() {
        return SubmissionAttributeTagRefFactory.getInstance();
    }
}
