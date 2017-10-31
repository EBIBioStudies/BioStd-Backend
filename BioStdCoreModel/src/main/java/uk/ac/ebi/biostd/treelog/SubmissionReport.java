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

package uk.ac.ebi.biostd.treelog;

import java.util.ArrayList;
import java.util.List;
import uk.ac.ebi.biostd.in.SubmissionMapping;

public class SubmissionReport {

    private LogNode log;
    private List<SubmissionMapping> sMap = new ArrayList<>();

    public LogNode getLog() {
        return log;
    }

    public void setLog(LogNode log) {
        this.log = log;
    }

    public List<SubmissionMapping> getMappings() {
        return sMap;
    }

    public void addSubmissionMapping(SubmissionMapping sm) {
        if (sMap == null) {
            sMap = new ArrayList<>();
        }

        sMap.add(sm);
    }

    public void setSubmissionMappings(List<SubmissionMapping> mp) {
        sMap = mp;
    }

}
