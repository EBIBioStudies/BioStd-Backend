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
import uk.ac.ebi.biostd.model.AbstractAttribute;
import uk.ac.ebi.biostd.model.trfactory.TagReferenceFactory;

public class VoidContext extends BlockContext {

    public VoidContext() {
        super(BlockType.NONE, null, null);
    }

    @Override
    public AbstractAttribute addAttribute(String nm, String val, Collection<? extends TagRef> tags) {
        throw new UnsupportedOperationException("addAttribute at VoidContext");
    }

    @Override
    public TagReferenceFactory<?> getAttributeTagRefFactory() {
        throw new UnsupportedOperationException("getAttributeTagRefFactory at VoidContext");
    }

    @Override
    public void parseFirstLine(List<String> parts, int lineNo) {
    }

    @Override
    public void parseLine(List<String> parts, int lineNo) {
    }

}
