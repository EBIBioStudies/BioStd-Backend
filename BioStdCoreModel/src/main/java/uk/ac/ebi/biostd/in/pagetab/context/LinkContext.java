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
import uk.ac.ebi.biostd.authz.TagRef;
import uk.ac.ebi.biostd.in.pagetab.ParserState;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.model.AbstractAttribute;
import uk.ac.ebi.biostd.model.Link;
import uk.ac.ebi.biostd.model.LinkAttribute;
import uk.ac.ebi.biostd.model.LinkAttributeTagRef;
import uk.ac.ebi.biostd.model.trfactory.LinkAttributeTagRefFactory;
import uk.ac.ebi.biostd.model.trfactory.LinkTagRefFactory;
import uk.ac.ebi.biostd.model.trfactory.TagReferenceFactory;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.LogNode.Level;

public class LinkContext extends VerticalBlockContext {


    private final Link link;


    public LinkContext(Link lnk, SubmissionInfo si, ParserState pars, LogNode sln) {
        super(BlockType.LINK, si, pars, sln);

        link = lnk;
    }

    @Override
    public void parseFirstLine(List<String> cells, int ln) {
        LogNode log = getContextLogNode();

        String nm = null;

        if (cells.size() > 1) {
            nm = cells.get(1).trim();
        }

        if (nm != null && nm.length() > 0) {
            link.setUrl(nm);
        } else {
            log.log(Level.ERROR, "(R" + ln + ",C2) URL missing");
        }

        link.setAccessTags(getParserState().getParser().processAccessTags(cells, ln, 3, log));
        link.setTagRefs(getParserState().getParser().processTags(cells, ln, 4, LinkTagRefFactory.getInstance(), log));
    }

    public Link getLink() {
        return link;
    }

    @Override
    public AbstractAttribute addAttribute(String nm, String val, Collection<? extends TagRef> tags) {
        LinkAttribute attr = new LinkAttribute();

        attr.setName(nm);
        attr.setValue(val);

        attr.setTagRefs((Collection<LinkAttributeTagRef>) tags);

        attr.setHost(link);
        link.addAttribute(attr);

        return attr;
    }


    @Override
    public TagReferenceFactory<?> getAttributeTagRefFactory() {
        return LinkAttributeTagRefFactory.getInstance();
    }

}
