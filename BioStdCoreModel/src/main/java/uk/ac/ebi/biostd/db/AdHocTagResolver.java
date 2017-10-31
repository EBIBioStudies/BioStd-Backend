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

package uk.ac.ebi.biostd.db;

import java.util.HashMap;
import java.util.Map;
import uk.ac.ebi.biostd.authz.AccessTag;
import uk.ac.ebi.biostd.authz.Classifier;
import uk.ac.ebi.biostd.authz.Tag;

public class AdHocTagResolver implements TagResolver {

    private final Map<String, Classifier> clsfMap = new HashMap<>();
    private final Map<String, AccessTag> accTagMap = new HashMap<>();

    private int idGen = 1;

    @Override
    public Tag getTagByName(String clsfName, String tagName) {
        Classifier clsf = clsfMap.get(clsfName);

        Tag t = null;

        if (clsf == null) {
            clsfMap.put(clsfName, clsf = new Classifier());

            clsf.setName(clsfName);
        } else {
            t = clsf.getTag(tagName);
        }

        if (t == null) {
            t = new Tag();

            t.setName(tagName);
            t.setId(idGen++);

            t.setClassifier(clsf);
            clsf.addTag(t);
        }

        return t;
    }

    @Override
    public AccessTag getAccessTagByName(String tagName) {
        AccessTag acct = accTagMap.get(tagName);

        if (acct == null) {
            accTagMap.put(tagName, acct = new AccessTag());

            acct.setId(idGen++);
            acct.setName(tagName);
        }

        return acct;
    }

}
