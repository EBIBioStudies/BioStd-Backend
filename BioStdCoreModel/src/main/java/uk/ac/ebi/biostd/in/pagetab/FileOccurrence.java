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

package uk.ac.ebi.biostd.in.pagetab;

import uk.ac.ebi.biostd.in.ElementPointer;
import uk.ac.ebi.biostd.model.FileRef;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.util.FilePointer;

public class FileOccurrence {


    private ElementPointer elementPointer;

    private FileRef fileRef;

    private LogNode secLogNode;

    private FilePointer filePointer;

    public ElementPointer getElementPointer() {
        return elementPointer;
    }

    public void setElementPointer(ElementPointer elementPointer) {
        this.elementPointer = elementPointer;
    }

    public FileRef getFileRef() {
        return fileRef;
    }

    public void setFileRef(FileRef fr) {
        fileRef = fr;
    }

    public LogNode getLogNode() {
        return secLogNode;
    }

    public void setLogNode(LogNode secLogNode) {
        this.secLogNode = secLogNode;
    }

    public FilePointer getFilePointer() {
        return filePointer;
    }

    public void setFilePointer(FilePointer filePointer) {
        this.filePointer = filePointer;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof FileOccurrence)) {
            return false;
        }

        FileRef fr2 = ((FileOccurrence) o).getFileRef();

        if (fileRef == null && fr2 == null) {
            return true;
        }

        if (fileRef != null) {
            if (fr2 == null) {
                return false;
            }

            if (fileRef.getName() == null && fr2.getName() == null) {
                return true;
            }

            if (fileRef.getName() != null) {
                return fileRef.getName().equals(fr2.getName());
            }
        } else {
            return fr2 == null;
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (fileRef == null || fileRef.getName() == null) {
            return 0;
        }

        return fileRef.getName().hashCode();
    }
}
