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
import uk.ac.ebi.biostd.model.AbstractAttribute;
import uk.ac.ebi.biostd.model.trfactory.TagReferenceFactory;
import uk.ac.ebi.biostd.treelog.LogNode;

public abstract class BlockContext {

    private final ParserState parserState;
    // private SectionOccurance parentSection;
    private final LogNode contextLogNode;
    private BlockType blockType;

    protected BlockContext(BlockType typ, ParserState ps, LogNode ctxln /*, SectionOccurance pc */) {
        blockType = typ;
        parserState = ps;
//  parentSection = pc;
        contextLogNode = ctxln;
    }
// private Section lastSection;

    public ParserState getParserState() {
        return parserState;
    }

// public Section getLastSection()
// {
//  return lastSection;
// }

    public BlockType getBlockType() {
        return blockType;
    }

// public void setLastSection(Section lastSection)
// {
//  this.lastSection = lastSection;
// }

    public void setBlockType(BlockType blockType) {
        this.blockType = blockType;
    }

    public abstract AbstractAttribute addAttribute(String nm, String val, Collection<? extends TagRef> tags);

    public abstract TagReferenceFactory<?> getAttributeTagRefFactory();
// public abstract AbstractAttribute addReference( String nm, String val, Collection<? extends TagRef> tags );

    public void finish() {
    }

    public abstract void parseFirstLine(List<String> parts, int lineNo);

    public abstract void parseLine(List<String> parts, int lineNo);

    protected LogNode getContextLogNode() {
        return contextLogNode;
    }

// public BlockContext getParentSection()
// {
//  return parentContext;
// }

    public enum BlockType {
        NONE, SUBMISSION, SECTION, FILE, LINK, SECTABLE, LINKTABLE, FILETABLE
    }

// public void setParentContext(SectionContext pCtx)
// {
//  parentContext = pCtx;
// }


}
