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

package uk.ac.ebi.biostd.in.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.ebi.biostd.authz.AccessTag;
import uk.ac.ebi.biostd.authz.Tag;
import uk.ac.ebi.biostd.db.TagResolver;
import uk.ac.ebi.biostd.in.PMDoc;
import uk.ac.ebi.biostd.in.Parser;
import uk.ac.ebi.biostd.in.ParserConfig;
import uk.ac.ebi.biostd.in.PathPointer;
import uk.ac.ebi.biostd.in.pagetab.ReferenceOccurrence;
import uk.ac.ebi.biostd.in.pagetab.SectionOccurrence;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.model.AbstractAttribute;
import uk.ac.ebi.biostd.model.Classified;
import uk.ac.ebi.biostd.model.FileRef;
import uk.ac.ebi.biostd.model.Link;
import uk.ac.ebi.biostd.model.Node;
import uk.ac.ebi.biostd.model.Qualifier;
import uk.ac.ebi.biostd.model.Section;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.out.json.JSONFormatter;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.LogNode.Level;

public class JSONReader extends Parser {

    public static final Pattern GeneratedAccNo = Pattern.compile(GeneratedAccNoRx);

    private final Matcher genAccNoMtch = GeneratedAccNo.matcher("");
    private final TagResolver tagResolver;
    private final ParserConfig conf;

    public JSONReader(TagResolver tgResl, ParserConfig pc) {
        tagResolver = tgResl;
        conf = pc;
    }

    public PMDoc parse(String txt, LogNode rln) {
        PMDoc doc = new PMDoc();

        LogNode sln = rln.branch("Parsing JSON body");

        JSONObject docobj = null;

        try {
            docobj = new JSONObject(txt);
        } catch (JSONException e) {
            sln.log(Level.ERROR, "JSON parsing failed: " + e.getMessage());
            return null;
        }

        Stack<String> path = new Stack<>();

        path.push("");

        Iterator<String> kitr = docobj.keys();

        while (kitr.hasNext()) {
            String key = kitr.next();
            Object val = docobj.get(key);

            path.push(key);

            if (key.startsWith("@")) {

                if (val instanceof JSONArray) {
                    JSONArray arr = (JSONArray) val;

                    for (int i = 0; i < arr.length(); i++) {
                        Object arval = arr.get(i);

                        if (arval instanceof JSONArray || arval instanceof JSONObject) {
                            path.push(String.valueOf(i));

                            sln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: scalar value expected");

                            path.pop();
                        } else {
                            doc.addHeader(key.substring(1), arval.toString());
                        }
                    }

                } else if (val instanceof JSONObject) {
                    sln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: scalar or array value expected");
                } else {
                    doc.addHeader(key.substring(1), val.toString());
                }

            } else if (JSONFormatter.submissionsProperty.equals(key)) {
                processSubmissions(val, doc, sln, path);
            } else {
                sln.log(Level.WARN, "Path '" + pathToString(path) + "' warning: unknown property. Ignoring");
            }

            path.pop();

        }

        return doc;
    }

    public void processSubmissions(Object jsroot, PMDoc doc, LogNode sln, Stack<String> path) {

        if (jsroot instanceof JSONArray) {
            JSONArray sbmarr = (JSONArray) jsroot;

            for (int i = 0; i < sbmarr.length(); i++) {
                try {
                    path.push(String.valueOf(i));

                    Object o = sbmarr.get(i);

                    if (!(o instanceof JSONObject)) {
                        sln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: expected JSON object");
                        continue;
                    }

                    SubmissionInfo si = processSubmission((JSONObject) o, sln, path);

                    if (si != null) {
                        finalizeSubmission(si);
                        doc.addSubmission(si);
                    }

                } finally {
                    path.pop();
                }

            }
        } else if (jsroot instanceof JSONObject) {
            SubmissionInfo si = processSubmission((JSONObject) jsroot, sln, path);

            if (si != null) {
                finalizeSubmission(si);
                doc.addSubmission(si);
            }

        } else {
            sln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: expected JSON object or array");
        }

    }

    private SubmissionInfo processSubmission(JSONObject obj, LogNode ln, Stack<String> path) {
        Submission sbm = new Submission();

        SubmissionInfo si = new SubmissionInfo(sbm);
        si.setElementPointer(new PathPointer(pathToString(path)));

        ln = ln.branch("Procesing submission");

        si.setLogNode(ln);

        Object acc = obj.opt(JSONFormatter.accNoProperty);

        if (acc != null) {
            ln.log(Level.INFO, "Submission accession no: " + acc);
        }

        Iterator<String> kitr = obj.keys();

        boolean typeOk = false;
        boolean rootOk = false;

        while (kitr.hasNext()) {
            String key = kitr.next();
            Object val = obj.get(key);

            try {
                path.push(key);

                switch (key) {
                    case JSONFormatter.typeProperty:

                        typeOk = true;

                        if (!(val instanceof String)) {
                            ln.log(Level.ERROR,
                                    "Path '" + pathToString(path) + "' error: invalid value. String expected");
                            continue;
                        }

                        if (!"submission".equalsIgnoreCase(val.toString())) {
                            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: type '" + val
                                    + "' isn't expected here. Must be 'submission'");
                            continue;
                        }

                        break;

                    case JSONFormatter.rootSecProperty:

                        rootOk = true;

                        if (!(val instanceof JSONObject)) {
                            ln.log(Level.ERROR,
                                    "Path '" + pathToString(path) + "' error: section object expected here");
                            continue;
                        }

                        SectionOccurrence rso = processSection((JSONObject) val, ln, path, Collections.emptyList(), si);

                        if (rso != null) {
                            sbm.setRootSection(rso.getSection());
                        }

                        break;

                    case JSONFormatter.accNoProperty:

                        if (!(val instanceof String)) {
                            ln.log(Level.ERROR,
                                    "Path '" + pathToString(path) + "' error: invalid value. String expected");
                            continue;
                        }

                        genAccNoMtch.reset((String) val);

                        if (genAccNoMtch.matches()) {
                            sbm.setAccNo(genAccNoMtch.group("tmpid"));
                            si.setAccNoPrefix(genAccNoMtch.group("pfx"));
                            si.setAccNoSuffix(genAccNoMtch.group("sfx"));
                        } else {
                            sbm.setAccNo((String) val);
                        }

                        break;

                    case JSONFormatter.idProperty:

                        if (!conf.isPreserveId()) {
                            break;
                        }

                        long id = 0;

                        if (val instanceof String) {
                            try {
                                id = Long.parseLong(val.toString());
                            } catch (Exception e) {
                                ln.log(Level.ERROR,
                                        "Path '" + pathToString(path) + "' error: invalid '" + JSONFormatter.idProperty
                                                + "' property value: " + val);
                                continue;
                            }
                        } else if (val instanceof Integer) {
                            id = (Integer) val;
                        } else if (val instanceof Long) {
                            id = (Long) val;
                        } else {
                            ln.log(Level.ERROR,
                                    "Path '" + pathToString(path) + "' error: invalid '" + JSONFormatter.idProperty
                                            + "' property value: " + val);
                            continue;
                        }

                        sbm.setId(id);

                        break;

                    default:

                        if (!processCommon(key, val, sbm, ln, path, si)) {
                            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: invalid property");
                            continue;
                        }

                        break;
                }


            } finally {
                path.pop();
            }
        }

        if (!typeOk) {
            ln.log(Level.WARN, "Object missing 'type' property. 'submission' assumed");
        }

        if (!rootOk) {
            ln.log(Level.ERROR, "Submission missing root section");
        }

        return si;
    }

    <NT> boolean processArray(Object val, NT nd, LogNode ln, Stack<String> path, SubmissionInfo si,
            NodeProcessor<NT> np) {
        if (!(val instanceof JSONArray)) {
            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: array expected here");
            return false;
        }

        boolean res = true;

        for (int j = 0; j < ((JSONArray) val).length(); j++) {
            try {
                path.push(String.valueOf(j));

                Object sso = ((JSONArray) val).get(j);

//    if( !( sso instanceof JSONObject ) )
//    {
//     ln.log(Level.ERROR, "Path '"+pathToString(path)+"' error: JSON object expected here" );
//     continue;
//    }

                res = res && np.process(sso, nd, ln, path, si);

            } finally {
                path.pop();
            }
        }

        return res;
    }

    private boolean processCommon(String key, Object val, Node obj, LogNode ln, Stack<String> path, SubmissionInfo si) {
        switch (key) {
            case JSONFormatter.attrubutesProperty:

                processArray(val, obj, ln, path, si, this::processAttribute);

                break;

            case JSONFormatter.classTagsProperty:

                processArray(val, obj, ln, path, si, this::processClassTag);

                break;

            case JSONFormatter.accTagsProperty:

                processArray(val, obj, ln, path, si, this::processAccessTag);

                break;

            default:
                return false;
        }

        return true;
    }

    private Qualifier processQualifier(Object tgjo, LogNode ln, Stack<String> path) {
        if (!(tgjo instanceof JSONObject)) {
            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: JSON object expected here");
            return null;
        }

        Iterator<String> kitr = ((JSONObject) tgjo).keys();

        boolean res = true;

        Qualifier q = new Qualifier();

        while (kitr.hasNext()) {
            String key = kitr.next();
            Object val = ((JSONObject) tgjo).get(key);
            try {
                path.push(key);

                switch (key) {
                    case JSONFormatter.nameProperty:

                        if (!(val instanceof String)) {
                            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: string value expected");
                            res = false;
                        } else {
                            q.setName((String) val);
                        }

                        break;

                    case JSONFormatter.valueProperty:

                        if (!(val instanceof String)) {
                            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: string value expected");
                            res = false;
                        } else {
                            q.setValue((String) val);
                        }

                        break;

                    default:

                        ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: invalid property '" + key + "'");
                        res = false;
                        break;
                }

            } finally {
                path.pop();
            }
        }

        if (q.getName() == null) {
            ln.log(Level.ERROR,
                    "Path '" + pathToString(path) + "' error: '" + JSONFormatter.nameProperty + "' property missing");
            res = false;
        }

        if (q.getValue() == null) {
            ln.log(Level.ERROR,
                    "Path '" + pathToString(path) + "' error: '" + JSONFormatter.valueProperty + "' property missing");
            res = false;
        }

        if (!res) {
            return null;
        }

        return q;
    }

    private boolean processNameQualifier(Object val, AbstractAttribute at, LogNode ln, Stack<String> path,
            SubmissionInfo s) {
        Qualifier q = processQualifier(val, ln, path);

        if (q == null) {
            return false;
        }

        at.addNameQualifier(q);

        return true;
    }

    private boolean processValueQualifier(Object val, AbstractAttribute at, LogNode ln, Stack<String> path,
            SubmissionInfo s) {
        Qualifier q = processQualifier(val, ln, path);

        if (q == null) {
            return false;
        }

        at.addValueQualifier(q);

        return true;
    }

    private boolean processAccessTag(Object val, Node nd, LogNode ln, Stack<String> path, SubmissionInfo s) {
        if (!(val instanceof String)) {
            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: string value expected");
            return false;
        }

        AccessTag at = tagResolver.getAccessTagByName((String) val);

        if (at == null) {
            ln.log(conf.missedAccessTagLL(),
                    "Path '" + pathToString(path) + "' error: access tag '" + val + "' can't be resolved");

            return conf.missedAccessTagLL() != Level.ERROR;
        } else {
//   ln.log(Level.INFO, "Access tag '" +val+"' resolved");

            nd.addAccessTag(at);
        }

        return true;
    }

    private boolean processAttribute(Object tgjo, Node nd, LogNode ln, Stack<String> path, SubmissionInfo si) {
        if (!(tgjo instanceof JSONObject)) {
            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: JSON object expected here");
            return false;
        }

        Iterator<String> kitr = ((JSONObject) tgjo).keys();

        boolean res = true;

        AbstractAttribute atr = nd.addAttribute(null, null);

        while (kitr.hasNext()) {
            String key = kitr.next();
            Object val = ((JSONObject) tgjo).get(key);
            try {
                path.push(key);

                switch (key) {
                    case JSONFormatter.nameProperty:

                        if (!(val instanceof String)) {
                            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: string value expected");
                            res = false;
                        } else {
                            atr.setName((String) val);
                        }

                        break;

                    case JSONFormatter.valueProperty:

                        if (!(val instanceof String)) {
                            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: string value expected");
                            res = false;
                        } else {
                            atr.setValue((String) val);
                        }

                        break;

                    case JSONFormatter.isRefProperty:

                        if (val instanceof Boolean) {
                            atr.setReference((Boolean) val);
                        } else if (val instanceof String) {
                            if ("true".equalsIgnoreCase((String) val)) {
                                atr.setReference(true);
                            } else if ("false".equalsIgnoreCase((String) val)) {
                                atr.setReference(false);
                            } else {
                                ln.log(Level.ERROR,
                                        "Path '" + pathToString(path) + "' error: invalid value. Boolean expected");
                                res = false;
                            }
                        } else {
                            ln.log(Level.ERROR,
                                    "Path '" + pathToString(path) + "' error: invalid value. Boolean expected");
                            res = false;
                        }

                        break;

                    case JSONFormatter.classTagsProperty:

                        res = res && processArray(val, atr, ln, path, si, this::processClassTag);

                        break;

                    case JSONFormatter.nmQualProperty:

                        res = res && processArray(val, atr, ln, path, si, this::processNameQualifier);

                        break;

                    case JSONFormatter.vlQualProperty:

                        res = res && processArray(val, atr, ln, path, si, this::processValueQualifier);

                        break;

                    default:

                        ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: invalid property '" + key + "'");
                        res = false;
                        break;
                }

            } finally {
                path.pop();
            }
        }

        if (atr.getName() == null) {
            ln.log(Level.ERROR,
                    "Path '" + pathToString(path) + "' error: '" + JSONFormatter.nameProperty + "' property missing");
            res = false;
        }

        if (atr.getValue() == null) {
            ln.log(Level.ERROR,
                    "Path '" + pathToString(path) + "' error: '" + JSONFormatter.valueProperty + "' property missing");
            res = false;
        }

        if (!res) {
            nd.removeAttribute(atr);
        } else if (atr.isReference()) {
            si.addReferenceOccurance(new PathPointer(pathToString(path)), atr, ln);
        }

        return res;
    }

    boolean processClassTag(Object tgjo, Classified nd, LogNode ln, Stack<String> path, SubmissionInfo s) {
        if (!(tgjo instanceof JSONObject)) {
            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: JSON object expected here");
            return false;
        }

//  ln = ln.branch("Processing tag");

        Iterator<String> kitr = ((JSONObject) tgjo).keys();

        String tagName = null;
        String clsfrName = null;
        String tgVal = null;

        while (kitr.hasNext()) {
            String key = kitr.next();
            Object val = ((JSONObject) tgjo).get(key);
            try {
                path.push(key);

                if (!(val instanceof String)) {
                    ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: string value expected");
                    return false;
                }

                switch (key) {
                    case JSONFormatter.tagProperty:

                        tagName = (String) val;

                        break;

                    case JSONFormatter.classifierProperty:

                        clsfrName = (String) val;

                        break;

                    case JSONFormatter.valueProperty:

                        tgVal = (String) val;

                        break;

                    default:

                        ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: invalid property '" + key + "'");
                        break;
                }

            } finally {
                path.pop();
            }
        }

        if (tagName == null) {
            ln.log(Level.ERROR,
                    "Path '" + pathToString(path) + "' error: '" + JSONFormatter.tagProperty + "' property missing");
            return false;
        }

        if (clsfrName == null) {
            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: '" + JSONFormatter.classifierProperty
                    + "' property missing");
            return false;
        }

        Tag tg = tagResolver.getTagByName(clsfrName, tagName);

        if (tg == null) {
            ln.log(conf.missedTagLL(), "Path '" + pathToString(path) + "' error: tag '" + clsfrName + ":" + tagName
                    + "' can't be resolved");

            return conf.missedTagLL() != Level.ERROR;
        } else {
//   ln.log(Level.INFO, "Tag '" + clsfrName +":"+tagName+"' resolved");

            nd.addTagRef(tg, tgVal);
        }

        return true;
    }

    private FileRef processFile(JSONObject obj, LogNode ln, Stack<String> path, SubmissionInfo si) {
        FileRef fr = new FileRef();

        ln = ln.branch("Processing file reference");

        Iterator<String> kitr = obj.keys();

        boolean nameOk = false;

        while (kitr.hasNext()) {
            String key = kitr.next();
            Object val = obj.get(key);
            try {
                path.push(key);

                switch (key) {
                    case JSONFormatter.pathProperty:

                        nameOk = true;

                        if (!(val instanceof String)) {
                            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: string value expected");
                            continue;
                        }

                        fr.setName((String) val);

                        break;

                    case JSONFormatter.sizeProperty:
                        break;

                    case JSONFormatter.typeProperty:
                        break;

                    default:

                        if (!processCommon(key, val, fr, ln, path, si)) {
                            ln.log(Level.ERROR,
                                    "Path '" + pathToString(path) + "' error: invalid property '" + key + "'");
                            continue;
                        }

                        break;
                }

            } finally {
                path.pop();
            }
        }

        if (!nameOk) {
            ln.log(Level.ERROR,
                    "Path '" + pathToString(path) + "' Object missing '" + JSONFormatter.pathProperty + "' property");
        }

        si.addFileOccurance(new PathPointer(pathToString(path)), fr, ln);

        return fr;
    }

    private Link processLink(JSONObject obj, LogNode ln, Stack<String> path, SubmissionInfo si) {
        Link lnk = new Link();

        ln = ln.branch("Processing link");

        Iterator<String> kitr = obj.keys();

        boolean urlOk = false;

        while (kitr.hasNext()) {
            String key = kitr.next();
            Object val = obj.get(key);
            try {
                path.push(key);

                switch (key) {
                    case JSONFormatter.urlProperty:

                        urlOk = true;

                        if (!(val instanceof String)) {
                            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: string value expected");
                            continue;
                        }

                        lnk.setUrl((String) val);

                        break;

                    default:

                        if (!processCommon(key, val, lnk, ln, path, si)) {
                            ln.log(Level.ERROR,
                                    "Path '" + pathToString(path) + "' error: invalid property '" + key + "'");
                            continue;
                        }

                        break;
                }

            } finally {
                path.pop();
            }
        }

        if (!urlOk) {
            ln.log(Level.ERROR,
                    "Path '" + pathToString(path) + "' Object missing '" + JSONFormatter.urlProperty + "' property");
        }

        return lnk;
    }

    private SectionOccurrence processSection(JSONObject obj, LogNode ln, Stack<String> path,
            List<SectionOccurrence> psecPath, SubmissionInfo si) {
        Section sec = new Section();

        SectionOccurrence secOc = new SectionOccurrence();

        secOc.setElementPointer(new PathPointer(pathToString(path)));
        secOc.setSection(sec);
        secOc.setSecLogNode(ln);

        if (psecPath.size() > 0) {
            secOc.setPosition(psecPath.get(psecPath.size() - 1).incSubSecCount());
        } else {
            secOc.setPosition(1);
        }

        List<SectionOccurrence> myPath = new ArrayList<>(psecPath);
        myPath.add(secOc);

        secOc.setPath(myPath);

        ln = ln.branch("Processing section '" + obj.opt(JSONFormatter.typeProperty) + "'");

        Iterator<String> kitr = obj.keys();

        boolean typeOk = false;

        while (kitr.hasNext()) {
            String key = kitr.next();
            Object val = obj.get(key);

            try {
                path.push(key);

                switch (key) {
                    case JSONFormatter.typeProperty:

                        typeOk = true;

                        if (!(val instanceof String)) {
                            ln.log(Level.ERROR,
                                    "Path '" + pathToString(path) + "' error: invalid value. String expected");
                            continue;
                        }

                        sec.setType((String) val);

                        break;

                    case JSONFormatter.accNoProperty:

                        if (!(val instanceof String)) {
                            ln.log(Level.ERROR,
                                    "Path '" + pathToString(path) + "' error: invalid value. String expected");
                            continue;
                        }

                        genAccNoMtch.reset((String) val);

                        if (genAccNoMtch.matches()) {
                            String pfx = genAccNoMtch.group("pfx");
                            String sfx = genAccNoMtch.group("sfx");

                            sec.setAccNo(genAccNoMtch.group("tmpid"));

                            if (pfx != null) {
                                pfx = pfx.trim();

                                if (pfx.length() > 0 && Character.isDigit(pfx.charAt(pfx.length() - 1))) {
                                    ln.log(Level.ERROR, "Path '" + pathToString(path)
                                            + "' error: Accession number prefix can't end with a digit '" + pfx + "'");
                                }
                            }

                            if (sfx != null) {
                                sfx = sfx.trim();

                                if (sfx.length() > 0 && Character.isDigit(sfx.charAt(0))) {
                                    ln.log(Level.ERROR, "Path '" + pathToString(path)
                                            + "' error: Accession number suffix can't start with a digit '" + sfx
                                            + "'");
                                }
                            }

                            secOc.setPrefix(pfx);
                            secOc.setSuffix(sfx);

                            si.addGlobalSection(secOc);
                        } else {
                            sec.setAccNo((String) val);
                        }

                        if (sec.getAccNo() != null) {
                            if (si.getSectionOccurance(sec.getAccNo()) != null) {
                                ln.log(Level.ERROR,
                                        "Accession number '" + sec.getAccNo() + "' is used by other section at " + secOc
                                                .getElementPointer());
                            }

                            si.addSectionOccurance(secOc);
                        }

                        break;

                    case JSONFormatter.subsectionsProperty:
                        try {
                            if (val instanceof JSONArray) {
                                for (int j = 0; j < ((JSONArray) val).length(); j++) {
                                    path.push(String.valueOf(j));

                                    Object sso = ((JSONArray) val).get(j);
                                    processSubSectionObject(sso, ln, path, myPath, si, sec);
                                }
                            } else {
                                processSubSectionObject(val, ln, path, myPath, si, sec);
                            }
                        } finally {
                            path.pop();
                        }

                        break;

                    case JSONFormatter.linksProperty:

                        if (!(val instanceof JSONArray)) {
                            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: array expected here");
                            continue;
                        }

                        for (int j = 0; j < ((JSONArray) val).length(); j++) {
                            try {
                                path.push(String.valueOf(j));

                                Object sso = ((JSONArray) val).get(j);

                                if (sso instanceof JSONObject) {
                                    sec.addLink(processLink((JSONObject) sso, ln, path, si));
                                } else if (sso instanceof JSONArray) {
                                    for (int k = 0; k < ((JSONArray) sso).length(); k++) {
                                        try {
                                            path.push(String.valueOf(k));

                                            Object tsso = ((JSONArray) sso).get(k);

                                            if (!(tsso instanceof JSONObject)) {
                                                ln.log(Level.ERROR,
                                                        "Path '" + pathToString(path) + "' JSON object expected");
                                            }

                                            Link lnk = processLink((JSONObject) tsso, ln, path, si);

                                            if (lnk != null) {
                                                lnk.setTableIndex(k);
                                                sec.addLink(lnk);
                                            }

                                        } finally {
                                            path.pop();
                                        }

                                    }
                                } else {
                                    ln.log(Level.ERROR,
                                            "Path '" + pathToString(path) + "' unexpected class: " + sso.getClass()
                                                    .getName());
                                }
                            } finally {
                                path.pop();
                            }
                        }

                        break;

                    case JSONFormatter.filesProperty:

                        if (!(val instanceof JSONArray)) {
                            ln.log(Level.ERROR, "Path '" + pathToString(path) + "' error: array expected here");
                            continue;
                        }

                        for (int j = 0; j < ((JSONArray) val).length(); j++) {
                            try {
                                path.push(String.valueOf(j));

                                Object sso = ((JSONArray) val).get(j);

                                if (sso instanceof JSONObject) {
                                    sec.addFileRef(processFile((JSONObject) sso, ln, path, si));
                                } else if (sso instanceof JSONArray) {
                                    for (int k = 0; k < ((JSONArray) sso).length(); k++) {
                                        try {
                                            path.push(String.valueOf(k));

                                            Object tsso = ((JSONArray) sso).get(k);

                                            if (!(tsso instanceof JSONObject)) {
                                                ln.log(Level.ERROR,
                                                        "Path '" + pathToString(path) + "' JSON object expected");
                                            }

                                            FileRef fr = processFile((JSONObject) tsso, ln, path, si);

                                            if (fr != null) {
                                                fr.setTableIndex(k);
                                                sec.addFileRef(fr);
                                            }

                                        } finally {
                                            path.pop();
                                        }

                                    }
                                } else {
                                    ln.log(Level.ERROR,
                                            "Path '" + pathToString(path) + "' unexpected class: " + sso.getClass()
                                                    .getName());
                                }
                            } finally {
                                path.pop();
                            }
                        }

                        break;

                    default:

                        if (!processCommon(key, val, sec, ln, path, si)) {
                            ln.log(Level.ERROR,
                                    "Path '" + pathToString(path) + "' error: invalid property '" + key + "'");
                            continue;
                        }

                        break;
                }


            } finally {
                path.pop();
            }
        }

        if (!typeOk) {
            ln.log(Level.ERROR,
                    "Path '" + pathToString(path) + "' Object missing '" + JSONFormatter.typeProperty + "' property");
        }

        return secOc;
    }

    private void processSubSectionObject(
        Object sso, LogNode ln, Stack<String> path, List<SectionOccurrence> myPath, SubmissionInfo si, Section sec) {
        if (sso instanceof JSONObject) {
            SectionOccurrence sbsec = processSection((JSONObject) sso, ln, path, myPath, si);

            if (sbsec != null) {
                if (sbsec.getSection().getAccNo() != null) {
                    si.addNonTableSection(sbsec);
                }

                sec.addSection(sbsec.getSection());
            }

        } else if (sso instanceof JSONArray) {
            for (int k = 0; k < ((JSONArray) sso).length(); k++) {
                try {
                    path.push(String.valueOf(k));

                    Object tsso = ((JSONArray) sso).get(k);

                    if (!(tsso instanceof JSONObject)) {
                        ln.log(Level.ERROR,
                                "Path '" + pathToString(path) + "' JSON object expected");
                    }

                    SectionOccurrence sbso = processSection((JSONObject) tsso, ln, path, myPath,
                            si);

                    if (sbso != null) {
                        sbso.getSection().setTableIndex(k);
                        sec.addSection(sbso.getSection());
                    }

                } finally {
                    path.pop();
                }

            }
        } else {
            ln.log(Level.ERROR,
                    "Path '" + pathToString(path) + "' unexpected class: " + sso.getClass()
                            .getName());
        }
    }

    private void finalizeSubmission(SubmissionInfo si) {
        if (si.getReferenceOccurrences() != null) {
            for (ReferenceOccurrence r : si.getReferenceOccurrences()) {
                SectionOccurrence soc = si.getSectionOccurance(r.getRef().getValue());
                if (soc == null) {
                    r.getLogNode().log(Level.ERROR,
                            r.getElementPointer() + " Invalid reference. Target doesn't exist: '" + r.getRef() + "'");
                } else {
                    r.setSection(soc.getSection());
                }
            }
        }
    }

    private String pathToString(Stack<String> pth) {
        StringBuilder sb = new StringBuilder(200);

        for (int i = 0; i < pth.size(); i++) {
            sb.append("/").append(pth.get(i));
        }

        return sb.toString();
    }

    interface NodeProcessor<NT> {

        boolean process(Object jsno, NT nd, LogNode ln, Stack<String> path, SubmissionInfo si);
    }
}
