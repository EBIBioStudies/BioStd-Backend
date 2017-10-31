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

import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.LogNode.Level;

public class ParserConfig {

    private LogNode.Level missedAccessTagLL = Level.WARN;
    private LogNode.Level missedTagLL = Level.WARN;
    private boolean multipleSubmissions = true;
    private boolean preserveId = false;

    public LogNode.Level missedAccessTagLL() {
        return missedAccessTagLL;
    }

    public LogNode.Level missedTagLL() {
        return missedTagLL;
    }

    public void missedAccessTagLL(LogNode.Level ll) {
        missedAccessTagLL = ll;
    }

    public void missedTagLL(LogNode.Level ll) {
        missedTagLL = ll;
    }

    public boolean isMultipleSubmissions() {
        return multipleSubmissions;
    }

    public void setMultipleSubmissions(boolean multipleSubmissions) {
        this.multipleSubmissions = multipleSubmissions;
    }

    public boolean isPreserveId() {
        return preserveId;
    }

    public void setPreserveId(boolean preserveId) {
        this.preserveId = preserveId;
    }

}
