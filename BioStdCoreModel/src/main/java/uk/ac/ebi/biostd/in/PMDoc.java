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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;

public class PMDoc {

    private List<SubmissionInfo> submissions;

    private Map<String, List<String>> headers;


    public void addSubmission(SubmissionInfo si) {
        if (submissions == null) {
            submissions = new ArrayList<>();
        }

        submissions.add(si);
    }

    public List<SubmissionInfo> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<SubmissionInfo> submissions) {
        this.submissions = submissions;
    }

    public void addHeader(String nm, String val) {
        List<String> vals = null;

        if (headers == null) {
            headers = new HashMap<>();
        } else {
            vals = headers.get(nm);
        }

        if (vals == null) {
            headers.put(nm, vals = new ArrayList<>());
        }

        vals.add(val);
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> parameters) {
        headers = parameters;
    }


}
