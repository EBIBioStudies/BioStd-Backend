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

package uk.ac.ebi.biostd.in;

import java.util.ArrayList;
import java.util.List;

public class SubmissionMapping {

    private AccessionMapping submissionMapping = new AccessionMapping();
    private List<AccessionMapping> sectionsMapping;


    public AccessionMapping getSubmissionMapping() {
        return submissionMapping;
    }

    public void setSubmissionMapping(AccessionMapping submissionMapping) {
        this.submissionMapping = submissionMapping;
    }

    public List<AccessionMapping> getSectionsMapping() {
        return sectionsMapping;
    }

    public void setSectionsMapping(List<AccessionMapping> sectionsMapping) {
        this.sectionsMapping = sectionsMapping;
    }

    public void addSectionMapping(AccessionMapping secMap) {
        if (sectionsMapping == null) {
            sectionsMapping = new ArrayList<>();
        }

        sectionsMapping.add(secMap);
    }
}
