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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import uk.ac.ebi.biostd.in.ElementPointer;
import uk.ac.ebi.biostd.model.AbstractAttribute;
import uk.ac.ebi.biostd.model.FileRef;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.treelog.LogNode;

public class SubmissionInfo {


    // private Map<String,SectionRef> sectionMap = new HashMap<String, SectionRef>();
    private final Collection<SectionOccurrence> globalSec = new ArrayList<>();
    private final Map<String, SectionOccurrence> localIdMap = new HashMap<>();
    private final Map<String, SectionOccurrence> parentSecMap = new HashMap<>();
    private Submission submission;
    private Submission originalSubmission;
    private ElementPointer elementPointer;
    private String accNoPrefix;
    private String accNoSuffix;
    private String accNoOriginal;
    private SectionOccurrence rootSectionOccurance;
    private Collection<FileOccurrence> files;
    private Collection<ReferenceOccurrence> refs;


    private LogNode logNode;


    public SubmissionInfo(Submission subm) {
        submission = subm;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public void addSectionOccurance(SectionOccurrence so) {
        localIdMap.put(so.getLocalId(), so);
    }

    public SectionOccurrence getSectionOccurance(String accNo) {
        return localIdMap.get(accNo);
    }

    public void addNonTableSection(SectionOccurrence so) {
        parentSecMap.put(so.getLocalId(), so);
    }

    public SectionOccurrence getNonTableSection(String pAcc) {
        return parentSecMap.get(pAcc);
    }

    public String getAccNoPrefix() {
        return accNoPrefix;
    }

    public void setAccNoPrefix(String accNoPrefix) {
        this.accNoPrefix = accNoPrefix;
    }

    public String getAccNoSuffix() {
        return accNoSuffix;
    }

    public void setAccNoSuffix(String accNoSuffix) {
        this.accNoSuffix = accNoSuffix;
    }


    public void addGlobalSection(SectionOccurrence sr) {
        globalSec.add(sr);
    }

    public Collection<SectionOccurrence> getGlobalSections() {
        return globalSec;
    }

    public LogNode getLogNode() {
        return logNode;
    }

    public void setLogNode(LogNode logNode) {
        this.logNode = logNode;
    }

    public void addReferenceOccurance(ElementPointer ep, AbstractAttribute ref, LogNode ln) {
        if (refs == null) {
            refs = new ArrayList<>();
        }

        ReferenceOccurrence ro = new ReferenceOccurrence();

        ro.setElementPointer(ep);
        ro.setRef(ref);
        ro.setLogNode(ln);

        refs.add(ro);
    }

    public void addFileOccurance(ElementPointer ep, FileRef ref, LogNode ln) {
        if (files == null) {
            files = new ArrayList<>();
        }

        FileOccurrence ro = new FileOccurrence();

        ro.setElementPointer(ep);
        ro.setFileRef(ref);
        ro.setLogNode(ln);

        files.add(ro);
    }

    public Collection<ReferenceOccurrence> getReferenceOccurrences() {
        return refs != null ? refs : Collections.emptyList();
    }

    public Collection<FileOccurrence> getFileOccurrences() {
        return files != null ? files : Collections.emptyList();
    }

    public Submission getOriginalSubmission() {
        return originalSubmission;
    }

    public void setOriginalSubmission(Submission originalSubmission) {
        this.originalSubmission = originalSubmission;
    }

    public String getAccNoOriginal() {
        return accNoOriginal;
    }

    public void setAccNoOriginal(String accNoOriginal) {
        this.accNoOriginal = accNoOriginal;
    }

    public SectionOccurrence getRootSectionOccurance() {
        return rootSectionOccurance;
    }

    public void setRootSectionOccurance(SectionOccurrence rootSectionOccurance) {
        this.rootSectionOccurance = rootSectionOccurance;
    }

    public ElementPointer getElementPointer() {
        return elementPointer;
    }

    public void setElementPointer(ElementPointer elementPointer) {
        this.elementPointer = elementPointer;
    }

}
