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

package uk.ac.ebi.biostd.in.pageml;

public enum PageMLElements {
    ROOT("pmdocument"), HEADER("header"), SUBMISSION("submission"), SUBMISSIONS("submissions"),
    SUBSECTIONS("subsections"), SECTION("section"), FILES("files"), FILE("file"), LINKS("links"), LINK("link"),
    ATTRIBUTES("attributes"), ATTRIBUTE("attribute"), TABLE("table"), NMQUALIFIER("nmqual"), VALQUALIFIER("valqual"),
    NAME("name"), VALUE("value"), PATH("path"), URL("url");

    private final String elementName;

    private PageMLElements(String el) {
        elementName = el;
    }

    public String getElementName() {
        return elementName;
    }
}
