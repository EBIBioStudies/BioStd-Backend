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

package uk.ac.ebi.biostd.out.cell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import uk.ac.ebi.biostd.authz.AccessTag;
import uk.ac.ebi.biostd.authz.TagRef;
import uk.ac.ebi.biostd.in.PMDoc;
import uk.ac.ebi.biostd.in.pagetab.PageTabElements;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.model.AbstractAttribute;
import uk.ac.ebi.biostd.model.Annotated;
import uk.ac.ebi.biostd.model.Classified;
import uk.ac.ebi.biostd.model.FileRef;
import uk.ac.ebi.biostd.model.Link;
import uk.ac.ebi.biostd.model.Node;
import uk.ac.ebi.biostd.model.Qualifier;
import uk.ac.ebi.biostd.model.Section;
import uk.ac.ebi.biostd.model.SecurityObject;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.out.DocumentFormatter;
import uk.ac.ebi.biostd.util.StringUtils;
import uk.ac.ebi.mg.spreadsheet.CellStream;

public class CellFormatter implements DocumentFormatter {

    static private final char[] tagChar2esc = new char[]{PageTabElements.ClassifierSeparator,
            PageTabElements.ValueTagSeparator, PageTabElements.TagSeparator1};
    private final CellStream cstr;
    private int counter = 1;

    public CellFormatter(CellStream s) {
        cstr = s;
    }

    @Override
    public void format(PMDoc document) throws IOException {
        cstr.start();

        header(document.getHeaders());

        for (SubmissionInfo s : document.getSubmissions()) {
            format(s.getSubmission());
        }

        cstr.finish();
    }

    private void header(Map<String, List<String>> hdrs) throws IOException {
        if (hdrs != null) {

            for (Map.Entry<String, List<String>> me : hdrs.entrySet()) {
                for (String val : me.getValue()) {
                    cstr.addCell(PageTabElements.DocParamPrefix + me.getKey());
                    cstr.addCell(val);
                    cstr.nextRow();
                }
            }
        }
    }

    private void format(Submission s) throws IOException {
        cstr.nextRow();
        cstr.addCell(PageTabElements.SubmissionKeyword);
        cstr.addCell(s.getAccNo());

        exportNodeTags(s);

        if (s.getTitle() != null) {
            cstr.addCell(Submission.canonicTitleAttribute);
            cstr.addCell(s.getTitle());
            cstr.nextRow();
        }

        if (s.isRTimeSet()) {
            cstr.addCell(Submission.canonicReleaseDateAttribute);
            cstr.addDateCell(s.getRTime() * 1000);
            cstr.nextRow();
        }

        if (s.getRootPath() != null) {
            cstr.addCell(Submission.canonicRootPathAttribute);
            cstr.addCell(s.getRootPath());
            cstr.nextRow();
        }

        exportAnnotation(s);

        exportSection(s.getRootSection());

    }

    private void exportNodeTags(Node nd) throws IOException {
        if ((nd.getAccessTags() == null || nd.getAccessTags().size() == 0) && (nd.getTagRefs() == null
                || nd.getTagRefs().size() == 0)) {
            cstr.nextRow();
            return;
        }

        exportAccessTags(nd);
        exportClassifTags(nd);
        cstr.nextRow();

    }

    private void exportAccessTags(SecurityObject so) throws IOException {
        if (so.getAccessTags() == null || so.getAccessTags().size() == 0) {
            cstr.nextCell();
            return;
        }

        if (so.getAccessTags().size() == 1) {
            cstr.addCell(so.getAccessTags().iterator().next().getName());
            return;
        }

        StringBuilder sb = new StringBuilder();

        for (AccessTag acct : so.getAccessTags()) {
            StringUtils.appendEscaped(sb, acct.getName(), PageTabElements.TagSeparator1, '\\');
            sb.append(PageTabElements.TagSeparator1);
        }

        sb.setLength(sb.length() - 1);

        cstr.addCell(sb.toString());
    }

    private void exportClassifTags(Classified clsf) throws IOException {
        if (clsf.getTagRefs() == null || clsf.getTagRefs().size() == 0) {
            cstr.nextCell();
            return;
        }

        StringBuilder sb = new StringBuilder();

        try {
            for (TagRef tg : clsf.getTagRefs()) {
                StringUtils.appendEscaped(sb, tg.getTag().getClassifier().getName(), tagChar2esc, '\\');
                sb.append(PageTabElements.ClassifierSeparator);
                StringUtils.appendEscaped(sb, tg.getTag().getName(), tagChar2esc, '\\');

                if (tg.getParameter() != null && tg.getParameter().length() > 0) {
                    sb.append(PageTabElements.ValueTagSeparator);
                    StringUtils.appendEscaped(sb, tg.getParameter(), tagChar2esc, '\\');
                }

                sb.append(PageTabElements.TagSeparator1);
            }
        } catch (IOException e) {
        }

        sb.setLength(sb.length() - 1);
        cstr.addCell(sb.toString());
    }

    private void exportTable(List<? extends Node> nodes, String titl, KeyExtactor kex) throws IOException {
        Map<String, AttrHdr> hdrMap = new LinkedHashMap<>();

        Integer int1 = new Integer(1);

        if (nodes != null) {
            for (Node n : nodes) {
                Map<String, Integer> atCntMap = new HashMap<>();

                if (n.getAttributes() != null) {
                    for (AbstractAttribute aa : n.getAttributes()) {
                        String name = aa.getName() + (aa.isReference() ? "_R" : "_A");

                        Integer cnt = atCntMap.get(name);

                        if (cnt == null) {
                            atCntMap.put(name, cnt = int1);

                            name = name + "_1";
                        } else {
                            cnt = cnt.intValue() + 1;
                            atCntMap.put(name, cnt);

                            name = name + "_" + cnt.intValue();
                        }

                        AttrHdr hdr = hdrMap.get(name);

                        if (hdr == null) {
                            hdrMap.put(name, hdr = new AttrHdr());
                            hdr.atName = aa.getName();
                            hdr.ref = aa.isReference();
                            hdr.ord = cnt;
                        }

                        if (aa.getValueQualifiers() != null) {
                            Map<String, Integer> qCntMap = new HashMap<>();

                            if (hdr.quals == null) {
                                hdr.quals = new LinkedHashMap<>();
                            }

                            for (Qualifier q : aa.getValueQualifiers()) {
                                String qname = q.getName();

                                Integer qcnt = qCntMap.get(name);

                                if (qcnt == null) {
                                    qCntMap.put(qname, qcnt = int1);

                                    qname = qname + "_1";
                                } else {
                                    qcnt = qcnt.intValue() + 1;
                                    qCntMap.put(qname, qcnt);

                                    qname = qname + "_" + qcnt.intValue();
                                }

                                AttrHdr qhdr = hdr.quals.get(qname);

                                if (qhdr == null) {
                                    hdr.quals.put(qname, qhdr = new AttrHdr());
                                    qhdr.atName = q.getName();
                                    qhdr.ord = qcnt.intValue();
                                }
                            }
                        }

                    }
                }
            }
        }

        cstr.nextRow();
        cstr.addCell(titl);

        for (AttrHdr ah : hdrMap.values()) {
            if (ah.ref) {
                cstr.addCell(String.valueOf(PageTabElements.RefOpen) + ah.atName + PageTabElements.RefClose);
            } else {
                cstr.addCell(ah.atName);
            }

            if (ah.quals != null) {
                for (AttrHdr qh : ah.quals.values()) {
                    cstr.addCell(String.valueOf(PageTabElements.ValueQOpen) + qh.atName + String
                            .valueOf(PageTabElements.ValueQClose));
                }
            }
        }

        if (nodes != null) {
            for (Node n : nodes) {
                cstr.nextRow();
                cstr.addCell(kex.getKey(n));

                for (AttrHdr ah : hdrMap.values()) {
                    AbstractAttribute cattr = null;

                    int aOrd = 1;
                    for (AbstractAttribute aa : n.getAttributes()) {
                        if (aa.getName().equals(ah.atName) && aa.isReference() == ah.ref) {
                            if (aOrd == ah.ord) {
                                cattr = aa;
                                cstr.addCell(aa.getValue());
                                break;
                            } else {
                                aOrd++;
                            }
                        }
                    }

                    if (cattr == null) {
                        cstr.nextCell();
                    }

                    if (ah.quals != null) {
                        for (AttrHdr qh : ah.quals.values()) {
                            if (cattr == null || cattr.getValueQualifiers() == null) {
                                cstr.nextCell();
                                continue;
                            }

                            int qOrd = 1;
                            Qualifier cqual = null;
                            for (Qualifier q : cattr.getValueQualifiers()) {
                                if (q.getName().equals(qh.atName)) {
                                    if (qOrd == qh.ord) {
                                        cqual = q;
                                        cstr.addCell(q.getValue());
                                        break;
                                    } else {
                                        qOrd++;
                                    }
                                }
                            }

                            if (cqual == null) {
                                cstr.nextCell();
                            }
                        }

                    }

                }
            }
        }

        cstr.nextRow();
    }

    private void exportAnnotation(Annotated ant) throws IOException {
        if (ant.getAttributes() == null) {
            return;
        }

        for (AbstractAttribute attr : ant.getAttributes()) {
            if (attr.isReference()) {
                cstr.addCell(String.valueOf(PageTabElements.RefOpen) + attr.getName() + PageTabElements.RefClose);
            } else {
                cstr.addCell(attr.getName());
            }

            cstr.addCell(attr.getValue());

            if (attr.getTagRefs() != null) {
                exportClassifTags(attr);
            }

            if (attr.getNameQualifiers() != null) {
                for (Qualifier q : attr.getNameQualifiers()) {
                    cstr.nextRow();
                    cstr.addCell(Character.toString(PageTabElements.NameQOpen) + q.getName() + Character
                            .toString(PageTabElements.NameQClose));
                    cstr.addCell(q.getValue());
                }
            }

            if (attr.getValueQualifiers() != null) {
                for (Qualifier q : attr.getValueQualifiers()) {
                    cstr.nextRow();
                    cstr.addCell(Character.toString(PageTabElements.ValueQOpen) + q.getName() + Character
                            .toString(PageTabElements.ValueQClose));
                    cstr.addCell(q.getValue());
                }
            }

            cstr.nextRow();

        }

    }

    private void exportSection(Section sec) throws IOException {

        cstr.nextRow();
        cstr.addCell(sec.getType());

        String acc = sec.getAccNo();

        if (sec.getSections() != null && sec.getSections().size() > 0 && (acc == null || acc.trim().length() == 0)) {
            acc = "$$$" + (counter++);
        }

        if (acc != null) {
            cstr.addCell((sec.isGlobal() ? "!" : "") + acc);
        } else {
            cstr.nextCell();
        }

        exportNodeTags(sec);

        exportAnnotation(sec);

        exportFileRefs(sec.getFileRefs());

        exportLinks(sec.getLinks());

        exportSubsections(sec);

    }

    private void exportSubsections(Section sec) throws IOException {
        List<Section> secs = sec.getSections();

        if (secs == null) {
            return;
        }

        List<Section> tbl = null;

        String lastSecType = null;

        for (Section sc : secs) {
            if (tbl != null && !sc.getType().equals(lastSecType)) {
                exportSectionTable(tbl, sec);

                tbl = null;
            }

            lastSecType = sc.getType();

            if (sc.getTableIndex() > 0) {
                if (tbl == null) {
                    tbl = new ArrayList<>();
                }

                tbl.add(sc);
            } else if (sc.getTableIndex() == 0) {
                if (tbl != null) {
                    exportSectionTable(tbl, sec);

                    tbl.clear();
                } else {
                    tbl = new ArrayList<>();
                }

                tbl.add(sc);
            } else {
                if (tbl != null) {
                    exportSectionTable(tbl, sec);

                    tbl = null;
                }

                exportSection(sc);
            }
        }

        if (tbl != null) {
            exportSectionTable(tbl, sec);
        }

    }

    private void exportSectionTable(List<Section> tbl, Section parent) throws IOException {
        String parentAcc = (parent.getParentSection() == null) ? "" : parent.getAccNo();

        exportTable(tbl, tbl.get(0).getType() + PageTabElements.TableOpen + parentAcc + PageTabElements.TableClose,
                n -> {
                    Section s = (Section) n;
                    if (s.getAccNo() == null) {
                        return "";
                    }
                    return s.isGlobal() ? "!" + s.getAccNo() : s.getAccNo();
                });
    }

    private void exportFileRefs(List<FileRef> frefs) throws IOException {
        if (frefs == null) {
            return;
        }

        List<FileRef> tbl = null;

        for (FileRef fr : frefs) {
            if (fr.getTableIndex() > 0) {
                if (tbl == null) {
                    tbl = new ArrayList<>();
                }

                tbl.add(fr);
            } else if (fr.getTableIndex() == 0) {
                if (tbl != null) {
                    exportFileTable(tbl);

                    tbl.clear();
                } else {
                    tbl = new ArrayList<>();
                }

                tbl.add(fr);
            } else {
                if (tbl != null) {
                    exportFileTable(tbl);

                    tbl = null;
                }

                exportFileRef(fr);
            }
        }

        if (tbl != null) {
            exportFileTable(tbl);
        }

    }

    private void exportFileRef(FileRef fr) throws IOException {
        cstr.nextRow();

        cstr.addCell(PageTabElements.FileKeyword);
        cstr.addCell(fr.getName());

        exportNodeTags(fr);

        exportAnnotation(fr);

    }

    private void exportFileTable(List<FileRef> tbl) throws IOException {
        exportTable(tbl, PageTabElements.FileTableKeyword, n -> ((FileRef) n).getName());
    }

    private void exportLinks(List<Link> links) throws IOException {
        if (links == null) {
            return;
        }

        List<Link> tbl = null;

        for (Link ln : links) {
            if (ln.getTableIndex() > 0) {
                if (tbl == null) {
                    tbl = new ArrayList<>();
                }

                tbl.add(ln);
            } else if (ln.getTableIndex() == 0) {
                if (tbl != null) {
                    exportLinkTable(tbl);

                    tbl.clear();
                } else {
                    tbl = new ArrayList<>();
                }

                tbl.add(ln);
            } else {
                if (tbl != null) {
                    exportLinkTable(tbl);

                    tbl = null;
                }

                exportLink(ln);
            }
        }

        if (tbl != null) {
            exportLinkTable(tbl);
        }

    }

    private void exportLink(Link ln) throws IOException {
        cstr.nextRow();

        cstr.addCell(PageTabElements.LinkKeyword);
        cstr.addCell(ln.getUrl());

        exportNodeTags(ln);

        exportAnnotation(ln);
    }

    private void exportLinkTable(List<Link> tbl) throws IOException {
        exportTable(tbl, PageTabElements.LinkTableKeyword, n -> ((Link) n).getUrl());
    }


    private interface KeyExtactor {

        String getKey(Node n);
    }

    private static class AttrHdr {

        LinkedHashMap<String, AttrHdr> quals;
        String atName;
        int ord = 0;
        boolean ref;

        @Override
        public int hashCode() {
            return atName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return atName.equals(((AttrHdr) obj).atName);
        }
    }


}
