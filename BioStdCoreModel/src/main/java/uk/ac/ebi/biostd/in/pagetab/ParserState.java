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

import java.util.regex.Matcher;

public class ParserState {

    private PageTabSyntaxParser parser;

    private Matcher nameQualifierMatcher;
    private Matcher valueQualifierMatcher;
    private Matcher referenceMatcher;
    private Matcher generatedAccNoMatcher;

    public Matcher getNameQualifierMatcher() {
        return nameQualifierMatcher;
    }

    public void setNameQualifierMatcher(Matcher nameQualifierMatcher) {
        this.nameQualifierMatcher = nameQualifierMatcher;
    }

    public Matcher getValueQualifierMatcher() {
        return valueQualifierMatcher;
    }

    public void setValueQualifierMatcher(Matcher valueQualifierMatcher) {
        this.valueQualifierMatcher = valueQualifierMatcher;
    }

    public Matcher getReferenceMatcher() {
        return referenceMatcher;
    }

    public void setReferenceMatcher(Matcher referenceMatcher) {
        this.referenceMatcher = referenceMatcher;
    }

    public Matcher getGeneratedAccNoMatcher() {
        return generatedAccNoMatcher;
    }

    public void setGeneratedAccNoMatcher(Matcher generatedAccNoMatcher) {
        this.generatedAccNoMatcher = generatedAccNoMatcher;
    }

    public PageTabSyntaxParser getParser() {
        return parser;
    }

    public void setParser(PageTabSyntaxParser parser) {
        this.parser = parser;
    }
}
