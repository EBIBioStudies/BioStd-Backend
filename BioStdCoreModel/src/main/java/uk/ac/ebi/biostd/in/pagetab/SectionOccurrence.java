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

import java.util.ArrayList;
import java.util.List;
import uk.ac.ebi.biostd.in.ElementPointer;
import uk.ac.ebi.biostd.model.Section;
import uk.ac.ebi.biostd.treelog.LogNode;

public class SectionOccurrence {

    private ElementPointer elementPointer;

    private Section section;

    private LogNode secLogNode;

    private String prefix;
    private String suffix;
    private String originalAccNo;
    private int position;
    private List<SectionOccurrence> path;
    private int subSecN = 0;

    public ElementPointer getElementPointer() {
        return elementPointer;
    }

    public void setElementPointer(ElementPointer elementPointer) {
        this.elementPointer = elementPointer;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public LogNode getSecLogNode() {
        return secLogNode;
    }

    public void setSecLogNode(LogNode secLogNode) {
        this.secLogNode = secLogNode;
    }

    public String getLocalId() {
        return section.getAccNo();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getOriginalAccNo() {
        return originalAccNo;
    }

    public void setOriginalAccNo(String originalAccNo) {
        this.originalAccNo = originalAccNo;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public List<SectionOccurrence> getPath() {
        return path;
    }

    public void setPath(List<SectionOccurrence> path) {
        this.path = path;
    }

    public void setParentPath(List<SectionOccurrence> pth) {
        path = new ArrayList<>(pth);

        path.add(this);
    }

    public int incSubSecCount() {
        return ++subSecN;
    }

    public int getSubSecCount() {
        return subSecN;
    }

}
