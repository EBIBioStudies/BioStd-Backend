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

public class PageTabElements {

    public static final char EscChar = '\\';
    public static final char TagSeparator1 = ',';
// public static final String TagSeparator2        = ",";

    public static final char NameQOpen = '(';
    public static final char NameQClose = ')';
    public static final char ValueQOpen = '[';
    public static final char ValueQClose = ']';
    public static final char RefOpen = '<';
    public static final char RefClose = '>';
    public static final char TableOpen = '[';
    public static final char TableClose = ']';


    // public static final String TagSeparatorRX        = "["+TagSeparator1+TagSeparator2+"]";
    public static final char ClassifierSeparator = ':';
    public static final char ValueTagSeparator = '=';
    public static final String CommentPrefix = "#";
    public static final String EscCommentPrefix = "" + EscChar + CommentPrefix;
    public static final String DocParamPrefix = "#@";

    public static final String NameQualifierRx =
            "\\s*\\" + NameQOpen + "\\s*(?<name>[^\\)]+)\\s*\\" + NameQClose + "\\s*";
    public static final String ValueQualifierRx =
            "\\s*\\" + ValueQOpen + "\\s*(?<name>[^\\]]+)\\s*\\" + ValueQClose + "\\s*";
    public static final String ReferenceRx = "\\s*\\" + RefOpen + "\\s*(?<name>[^\\>]+)\\s*\\" + RefClose + "\\s*";
    public static final String TableBlockRx =
            "\\s*(?<name>[^\\" + TableOpen + "]+)\\" + TableOpen + "\\s*(?<parent>[^\\" + TableClose + "]+)?\\s*\\"
                    + TableClose + "\\s*";

    public static final String SubmissionKeyword = "Submission";
    public static final String FileKeyword = "File";
    public static final String LinkKeyword = "Link";
    public static final String LinkTableKeyword = "Links";
    public static final String FileTableKeyword = "Files";
}
