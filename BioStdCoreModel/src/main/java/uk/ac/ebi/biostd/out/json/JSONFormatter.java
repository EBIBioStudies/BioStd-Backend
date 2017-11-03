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

package uk.ac.ebi.biostd.out.json;


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.ac.ebi.biostd.authz.AccessTag;
import uk.ac.ebi.biostd.authz.TagRef;
import uk.ac.ebi.biostd.in.PMDoc;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.model.AbstractAttribute;
import uk.ac.ebi.biostd.model.Annotated;
import uk.ac.ebi.biostd.model.Classified;
import uk.ac.ebi.biostd.model.FileRef;
import uk.ac.ebi.biostd.model.Link;
import uk.ac.ebi.biostd.model.Qualifier;
import uk.ac.ebi.biostd.model.Section;
import uk.ac.ebi.biostd.model.SecurityObject;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.out.AbstractFormatter;

public class JSONFormatter implements AbstractFormatter {

    public static final String submissionsProperty = "submissions";
    public static final String rootSecProperty = "section";
    public static final String attrubutesProperty = "attributes";
    public static final String idProperty = "id";
    public static final String ctimeProperty = "ctime";
    public static final String mtimeProperty = "mtime";
    public static final String rtimeProperty = "rtime";
    public static final String accNoProperty = "accno";
    public static final String relPathProperty = "relPath";
    public static final String accTagsProperty = "accessTags";
    public static final String classTagsProperty = "tags";
    public static final String tagProperty = "tag";
    public static final String classifierProperty = "classifier";
    public static final String nameProperty = "name";
    public static final String valueProperty = "value";
    public static final String nmQualProperty = "nmqual";
    public static final String vlQualProperty = "valqual";
    public static final String isRefProperty = "isReference";
    public static final String typeProperty = "type";
    public static final String sizeProperty = "size";
    public static final String subsectionsProperty = "subsections";
    public static final String filesProperty = "files";
    public static final String linksProperty = "links";
    public static final String urlProperty = "url";
    public static final String pathProperty = "path";
    public static final String seckeyProperty = "seckey";

    public final static String dateFotmat = "yyyy-MM-dd";


    private Appendable outStream;

    private DateFormat dateFmt;

    private boolean cutTech = false;

    public JSONFormatter() {
    }

    public JSONFormatter(Appendable o, boolean cut) {
        outStream = o;
        cutTech = cut;
    }


    @Override
    public void format(PMDoc document) throws IOException {
        header(document.getHeaders(), outStream);

        boolean first = true;

        for (SubmissionInfo s : document.getSubmissions()) {
            if (first) {
                first = false;
            } else {
                separator(outStream);
            }

            format(s.getSubmission(), outStream);
        }

        footer(outStream);
    }


    @Override
    public void header(Map<String, List<String>> hdrs, Appendable out) throws IOException {
        out.append("{\n");

        if (hdrs != null) {
            for (Map.Entry<String, List<String>> me : hdrs.entrySet()) {
                if (me.getValue().size() == 1) {
                    out.append(JSONObject.quote("@" + me.getKey())).append(": ")
                            .append(JSONObject.quote(me.getValue().get(0))).append(",\n");
                } else if (me.getValue().size() > 1) {
                    out.append(JSONObject.quote("@" + me.getKey())).append(": [\n");

                    for (String val : me.getValue()) {
                        out.append(JSONObject.quote(val)).append(",\n");
                    }

                    out.append("],\n");
                }
            }
        }

        out.append("\"").append(submissionsProperty).append("\" : [\n");

    }


    @Override
    public void footer(Appendable out) throws IOException {
        out.append("\n]\n}");
    }


    @Override
    public void separator(Appendable out) throws IOException {
        out.append(",\n");
    }


    @Override
    public void comment(String comment, Appendable out) throws IOException {
    }


    @Override
    public void format(Submission s, Appendable out) throws IOException {
        JSONObject sbm = new JSONObject();

        sbm.put(typeProperty, "submission");

        if (!cutTech) {
            sbm.put(idProperty, s.getId());
        }

        if (s.getAccNo() != null) {
            sbm.put(accNoProperty, s.getAccNo());
        }

        String str = s.getRelPath();
        if (str != null && str.length() > 0 && !cutTech) {
            sbm.put(relPathProperty, str);
        }

        Map<String, String> auxAttrMap = new HashMap<>();

        if (s.getTitle() != null) {
            auxAttrMap.put(Submission.canonicTitleAttribute, s.getTitle());
        }

        if (!cutTech) {
            sbm.put(ctimeProperty, String.valueOf(s.getCTime()));
            sbm.put(mtimeProperty, String.valueOf(s.getMTime()));

            if (s.isRTimeSet()) {
                sbm.put(rtimeProperty, String.valueOf(s.getRTime()));
            }

            if (s.getSecretKey() != null) {
                sbm.put(seckeyProperty, s.getSecretKey());
            }
        }

        if (s.isRTimeSet()) {
            if (dateFmt == null) {
                dateFmt = new SimpleDateFormat(dateFotmat);
            }

            auxAttrMap.put(Submission.canonicReleaseDateAttribute, dateFmt.format(new Date(s.getRTime() * 1000)));
        }

        if (s.getRootPath() != null) {
            auxAttrMap.put(Submission.canonicRootPathAttribute, s.getRootPath());
        }

        appendAttributes(sbm, auxAttrMap, s);

        appendAccessTags(sbm, s);

        if (s.getOwner() != null && !cutTech) {
            JSONArray arr = sbm.optJSONArray(accTagsProperty);

            if (arr == null) {
                arr = new JSONArray();
                sbm.put(accTagsProperty, arr);
            }

            String txtId = s.getOwner().getEmail();

            if (txtId == null) {
                txtId = s.getOwner().getLogin();
            }

            arr.put("~" + txtId);
            arr.put("#" + String.valueOf(s.getOwner().getId()));
        }

        appendTags(sbm, s);

        sbm.put(rootSecProperty, appendSection(new JSONObject(), s.getRootSection()));

        out.append(sbm.toString(1));
    }


    private JSONObject appendSection(JSONObject jsobj, Section sec) {
        jsobj.put(typeProperty, sec.getType());

        if (sec.getAccNo() != null) {
            jsobj.put(accNoProperty, sec.getAccNo());
        }

        appendAttributes(jsobj, sec);

        appendAccessTags(jsobj, sec);

        appendTags(jsobj, sec);

        if (sec.getSections() != null && sec.getSections().size() > 0) {
            JSONArray sbsarr = new JSONArray();

            JSONArray sbstblarr = null;

            for (Section sbs : sec.getSections()) {
                JSONObject jssbs = new JSONObject();

                appendSection(jssbs, sbs);

                if (sbs.getTableIndex() >= 0) {
                    if (sbstblarr == null || sbs.getTableIndex() == 0) {
                        sbstblarr = new JSONArray();
                        sbsarr.put(sbstblarr);
                    }

                    sbstblarr.put(jssbs);
                } else {
                    sbstblarr = null;
                    sbsarr.put(jssbs);
                }

            }

            jsobj.put(subsectionsProperty, sbsarr);
        }

        appendFiles(jsobj, sec);
        appendLinks(jsobj, sec);

        return jsobj;
    }


    private void appendFiles(JSONObject jsobj, Section s) {
        if (s.getFileRefs() == null || s.getFileRefs().size() == 0) {
            return;
        }

        JSONArray flarr = new JSONArray();

        JSONArray fltblarr = null;

        for (FileRef fr : s.getFileRefs()) {
            JSONObject jsfl = new JSONObject();

            jsfl.put(pathProperty, fr.getPath() != null ? fr.getPath() : fr.getName());

            if (fr.getSize() != 0) {
                jsfl.put(sizeProperty, fr.getSize());
                jsfl.put(typeProperty, fr.isDirectory() ? "directory" : "file");
            }

            appendAttributes(jsfl, fr);
            appendAccessTags(jsfl, fr);
            appendTags(jsfl, fr);

            if (fr.getTableIndex() >= 0) {
                if (fltblarr == null || fr.getTableIndex() == 0) {
                    fltblarr = new JSONArray();
                    flarr.put(fltblarr);
                }

                fltblarr.put(jsfl);
            } else {
                fltblarr = null;
                flarr.put(jsfl);
            }

        }

        jsobj.put(filesProperty, flarr);
    }

    private void appendLinks(JSONObject jsobj, Section s) {
        if (s.getLinks() == null || s.getLinks().size() == 0) {
            return;
        }

        JSONArray lnarr = new JSONArray();

        JSONArray lntblarr = null;

        for (Link ln : s.getLinks()) {
            JSONObject jsln = new JSONObject();

            jsln.put(urlProperty, ln.getUrl());

            appendAttributes(jsln, ln);
            appendAccessTags(jsln, ln);
            appendTags(jsln, ln);

            if (ln.getTableIndex() >= 0) {
                if (lntblarr == null || ln.getTableIndex() == 0) {
                    lntblarr = new JSONArray();
                    lnarr.put(lntblarr);
                }

                lntblarr.put(jsln);
            } else {
                lntblarr = null;
                lnarr.put(jsln);
            }

        }

        jsobj.put(linksProperty, lnarr);
    }

    private void appendAttributes(JSONObject jsobj, Annotated an) {
        appendAttributes(jsobj, null, an);
    }

    private void appendAttributes(JSONObject jsobj, Map<String, String> aux, Annotated an) {
        if ((an.getAttributes() == null || an.getAttributes().size() == 0) && (aux == null || aux.size() == 0)) {
            return;
        }

        JSONArray tgarr = new JSONArray();

        if (aux != null) {
            for (Map.Entry<String, String> me : aux.entrySet()) {
                JSONObject jsat = new JSONObject();

                jsat.put(nameProperty, me.getKey());
                jsat.put(valueProperty, me.getValue());

                tgarr.put(jsat);
            }
        }

        for (AbstractAttribute aat : an.getAttributes()) {
            JSONObject jsat = new JSONObject();

            jsat.put(nameProperty, aat.getName());
            jsat.put(valueProperty, aat.getValue());

            if (aat.isReference()) {
                jsat.put(isRefProperty, true);
            }

            appendTags(jsat, aat);

            if (aat.getNameQualifiers() != null && aat.getNameQualifiers().size() != 0) {
                JSONArray nqarr = new JSONArray();

                for (Qualifier q : aat.getNameQualifiers()) {
                    JSONObject nq = new JSONObject();

                    nq.put(nameProperty, q.getName());
                    nq.put(valueProperty, q.getValue());

                    nqarr.put(nq);
                }

                jsat.put(nmQualProperty, nqarr);
            }

            if (aat.getValueQualifiers() != null && aat.getValueQualifiers().size() != 0) {
                JSONArray nqarr = new JSONArray();

                for (Qualifier q : aat.getValueQualifiers()) {
                    JSONObject nq = new JSONObject();

                    nq.put(nameProperty, q.getName());
                    nq.put(valueProperty, q.getValue());

                    nqarr.put(nq);
                }

                jsat.put(vlQualProperty, nqarr);
            }

            tgarr.put(jsat);
        }

        jsobj.put(attrubutesProperty, tgarr);
    }

    private void appendAccessTags(JSONObject jsobj, SecurityObject an) {
        if (an.getAccessTags() == null || an.getAccessTags().size() == 0) {
            return;
        }

        JSONArray tgarr = new JSONArray();

        for (AccessTag atg : an.getAccessTags()) {
            tgarr.put(atg.getName());
        }

        jsobj.put(accTagsProperty, tgarr);
    }

    private void appendTags(JSONObject jsobj, Classified an) {
        if (an.getTagRefs() == null || an.getTagRefs().size() == 0) {
            return;
        }

        JSONArray tgarr = new JSONArray();

        for (TagRef atg : an.getTagRefs()) {
            JSONObject jstr = new JSONObject();

            jstr.put(classifierProperty, atg.getTag().getClassifier().getName());
            jstr.put(tagProperty, atg.getTag().getName());

            if (atg.getParameter() != null) {
                jstr.put(valueProperty, atg.getParameter());
            }

            tgarr.put(jstr);
        }

        jsobj.put(classTagsProperty, tgarr);
    }


}
