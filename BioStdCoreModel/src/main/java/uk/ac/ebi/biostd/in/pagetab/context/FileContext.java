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
import uk.ac.ebi.biostd.in.CellPointer;
import uk.ac.ebi.biostd.in.pagetab.ParserState;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.model.AbstractAttribute;
import uk.ac.ebi.biostd.model.FileAttribute;
import uk.ac.ebi.biostd.model.FileAttributeTagRef;
import uk.ac.ebi.biostd.model.FileRef;
import uk.ac.ebi.biostd.model.trfactory.FileAttributeTagRefFactory;
import uk.ac.ebi.biostd.model.trfactory.FileTagRefFactory;
import uk.ac.ebi.biostd.model.trfactory.TagReferenceFactory;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.LogNode.Level;

public class FileContext extends VerticalBlockContext {

    private final FileRef fileRef;

    public FileContext(FileRef fr, SubmissionInfo si, ParserState pars, LogNode sln /*, BlockContext pc */) {
        super(BlockType.FILE, si, pars, sln);

        fileRef = fr;
    }

    @Override
    public void parseFirstLine(List<String> cells, int ln) {
        LogNode log = getContextLogNode();

        String nm = null;

        if (cells.size() > 1) {
            nm = cells.get(1).trim();
        }

        if (nm != null && nm.length() > 0) {
            fileRef.setName(nm);
        } else {
            log.log(Level.ERROR, "(R" + ln + ",C2) File name missing");
        }

        fileRef.setAccessTags(getParserState().getParser().processAccessTags(cells, ln, 3, log));
        fileRef.setTagRefs(
                getParserState().getParser().processTags(cells, ln, 4, FileTagRefFactory.getInstance(), log));

        getSubmissionInfo().addFileOccurance(new CellPointer(ln, 1), fileRef, log);
    }

    public FileRef getFileRef() {
        return fileRef;
    }

    @Override
    public AbstractAttribute addAttribute(String nm, String val, Collection<? extends TagRef> tags) {
        FileAttribute attr = new FileAttribute();

        attr.setName(nm);
        attr.setValue(val);

        attr.setTagRefs((Collection<FileAttributeTagRef>) tags);

        attr.setHost(fileRef);
        fileRef.addAttribute(attr);

        return attr;
    }


    @Override
    public TagReferenceFactory<?> getAttributeTagRefFactory() {
        return FileAttributeTagRefFactory.getInstance();
    }


}
