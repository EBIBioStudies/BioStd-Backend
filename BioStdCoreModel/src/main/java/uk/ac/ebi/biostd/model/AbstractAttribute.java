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

package uk.ac.ebi.biostd.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import uk.ac.ebi.biostd.authz.TagRef;

@MappedSuperclass
abstract public class AbstractAttribute implements NameValuePair, Classified {

    private final static String QUALIFIERS_SEPARATOR = ";";
    private final static String QUALIFIER_VALUE_SEPARATOR = "=";

    private static final Pattern qStrSplit = Pattern.compile("(?<!\\\\)" + QUALIFIERS_SEPARATOR);
    private static final Pattern unescQS = Pattern.compile("\\\\" + QUALIFIERS_SEPARATOR);
    private static final Pattern unescQV = Pattern.compile("\\\\" + QUALIFIER_VALUE_SEPARATOR);
    private static final Pattern escQS = Pattern.compile(QUALIFIERS_SEPARATOR);
    private static final Pattern escQV = Pattern.compile(QUALIFIER_VALUE_SEPARATOR);
    private boolean isReference = false;
    private List<Qualifier> nameQualifiers;
    private List<Qualifier> valueQualifiers;
    private String nameQualifierString;
    private String valueQualifierString;
    private long id;
    private String name;
    private String value;

    public AbstractAttribute() {
    }

    public AbstractAttribute(String name2, String value2) {
        setName(name2);
        setValue(value2);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isReference() {
        return isReference;
    }

    public void setReference(boolean rf) {
        isReference = rf;
    }

    @Override
    @Lob
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Transient
    public List<Qualifier> getNameQualifiers() {
        if (nameQualifiers != null) {
            return nameQualifiers;
        }

        if (nameQualifierString == null) {
            return null;
        }

        nameQualifiers = strToQualifiers(nameQualifierString);

        return nameQualifiers;
    }

    public void setNameQualifiers(List<Qualifier> qs) {
        nameQualifiers = qs;

        nameQualifierString = qualifiersToStr(qs);
    }

    public void addNameQualifier(Qualifier q) {
        nameQualifierString = addQualifierToStr(nameQualifierString, q);

        if (nameQualifiers == null) {
            nameQualifiers = new ArrayList<>();
        }

        nameQualifiers.add(q);
    }

    @Transient
    public List<Qualifier> getValueQualifiers() {
        if (valueQualifiers != null) {
            return valueQualifiers;
        }

        if (valueQualifierString == null) {
            return null;
        }

        valueQualifiers = strToQualifiers(valueQualifierString);

        return valueQualifiers;
    }

    public void setValueQualifiers(List<Qualifier> qs) {
        valueQualifiers = qs;

        valueQualifierString = qualifiersToStr(qs);
    }

    public void addValueQualifier(Qualifier q) {
        valueQualifierString = addQualifierToStr(valueQualifierString, q);

        if (valueQualifiers == null) {
            valueQualifiers = new ArrayList<>();
        }

        valueQualifiers.add(q);
    }

    @Override
    @Lob
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    private String addQualifierToStr(String str, Qualifier q) {
        StringBuilder sb = new StringBuilder();

        Matcher qvMatcher = escQV.matcher("");
        Matcher qsMatcher = escQS.matcher("");

        if (str != null && str.length() > 0) {
            sb.append(str).append(QUALIFIERS_SEPARATOR);
        }

        sb.append(qsMatcher.reset(qvMatcher.reset(q.getName()).replaceAll("\\\\" + QUALIFIER_VALUE_SEPARATOR))
                .replaceAll("\\\\" + QUALIFIERS_SEPARATOR));
        sb.append(QUALIFIER_VALUE_SEPARATOR);
        sb.append(qsMatcher.reset(q.getValue()).replaceAll("\\\\" + QUALIFIERS_SEPARATOR));

        return sb.toString();
    }

    private String qualifiersToStr(Collection<Qualifier> qs) {
        if (qs == null || qs.size() == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        Matcher qvMatcher = escQV.matcher("");
        Matcher qsMatcher = escQS.matcher("");

        for (Qualifier q : qs) {
            sb.append(qsMatcher.reset(qvMatcher.reset(q.getName()).replaceAll("\\\\" + QUALIFIER_VALUE_SEPARATOR))
                    .replaceAll("\\\\" + QUALIFIERS_SEPARATOR));
            sb.append(QUALIFIER_VALUE_SEPARATOR);
            sb.append(qsMatcher.reset(q.getValue()).replaceAll(QUALIFIERS_SEPARATOR));
            sb.append(QUALIFIERS_SEPARATOR);
        }

        sb.setLength(sb.length() - QUALIFIERS_SEPARATOR.length());

        return sb.toString();
    }

    private List<Qualifier> strToQualifiers(String str) {
        String[] qus = qStrSplit.split(str);

        List<Qualifier> res = new ArrayList<>(qus.length);

        for (String s : qus) {
            s = unescQS.matcher(s).replaceAll(QUALIFIERS_SEPARATOR);

            String nm = s;
            String vl = null;

            int pos = 0;

            while (pos < s.length() && (pos = s.indexOf(QUALIFIER_VALUE_SEPARATOR, pos)) != -1) {
                if (pos == 0) {
                    break;
                }

                if (s.charAt(pos - 1) != '\\') {
                    nm = unescQV.matcher(s.substring(0, pos)).replaceAll(QUALIFIER_VALUE_SEPARATOR);
                    vl = s.substring(pos + QUALIFIER_VALUE_SEPARATOR.length());

                    break;
                }

                pos = pos + QUALIFIER_VALUE_SEPARATOR.length();
            }

            res.add(new Qualifier(nm, vl));
        }

        return res;
    }


    @Override
    @Transient
    public abstract Collection<? extends TagRef> getTagRefs();

    @Override
    @Transient
    public String getEntityClass() {
        if (getTagRefs() == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (TagRef t : getTagRefs()) {
            sb.append(t.getTag().getClassifier().getName()).append(":").append(t.getTag().getName());

            if (t.getParameter() != null && t.getParameter().length() != 0) {
                sb.append("=").append(t.getParameter());
            }

            sb.append(",");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(name);

        if (getNameQualifiers() != null && getNameQualifiers().size() > 0) {
            sb.append('<');

            for (Qualifier q : getNameQualifiers()) {
                sb.append(q.getName()).append('=').append(q.getValue()).append(';');
            }

            sb.setLength(sb.length() - 1);

            sb.append('>');
        }

        sb.append('=').append(value);

        if (getValueQualifiers() != null && getValueQualifiers().size() > 0) {
            sb.append('[');

            for (Qualifier q : getValueQualifiers()) {
                sb.append(q.getName()).append('=').append(q.getValue()).append(';');
            }

            sb.setLength(sb.length() - 1);

            sb.append(']');
        }

        return sb.toString();

    }

    @Lob
    private String getNameQualifierString() {
        return nameQualifierString;
    }

    private void setNameQualifierString(String nameQualifierString) {
        this.nameQualifierString = nameQualifierString;
    }

    @Lob
    private String getValueQualifierString() {
        return valueQualifierString;
    }

    private void setValueQualifierString(String valueQualifierString) {
        this.valueQualifierString = valueQualifierString;
    }
}
