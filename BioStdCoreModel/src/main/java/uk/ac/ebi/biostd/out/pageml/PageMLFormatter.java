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

package uk.ac.ebi.biostd.out.pageml;

import static uk.ac.ebi.biostd.in.pageml.PageMLAttributes.ACCESS;
import static uk.ac.ebi.biostd.in.pageml.PageMLAttributes.ACCNO;
import static uk.ac.ebi.biostd.in.pageml.PageMLAttributes.ACCNOGLOBAL;
import static uk.ac.ebi.biostd.in.pageml.PageMLAttributes.CLASS;
import static uk.ac.ebi.biostd.in.pageml.PageMLAttributes.ID;
import static uk.ac.ebi.biostd.in.pageml.PageMLAttributes.RELPATH;
import static uk.ac.ebi.biostd.in.pageml.PageMLAttributes.SIZE;
import static uk.ac.ebi.biostd.in.pageml.PageMLAttributes.TYPE;
import static uk.ac.ebi.biostd.in.pageml.PageMLElements.ATTRIBUTE;
import static uk.ac.ebi.biostd.in.pageml.PageMLElements.ATTRIBUTES;
import static uk.ac.ebi.biostd.in.pageml.PageMLElements.FILE;
import static uk.ac.ebi.biostd.in.pageml.PageMLElements.FILES;
import static uk.ac.ebi.biostd.in.pageml.PageMLElements.HEADER;
import static uk.ac.ebi.biostd.in.pageml.PageMLElements.LINK;
import static uk.ac.ebi.biostd.in.pageml.PageMLElements.LINKS;
import static uk.ac.ebi.biostd.in.pageml.PageMLElements.ROOT;
import static uk.ac.ebi.biostd.in.pageml.PageMLElements.SECTION;
import static uk.ac.ebi.biostd.in.pageml.PageMLElements.SUBMISSION;
import static uk.ac.ebi.biostd.in.pageml.PageMLElements.SUBMISSIONS;
import static uk.ac.ebi.biostd.in.pageml.PageMLElements.SUBSECTIONS;
import static uk.ac.ebi.biostd.in.pageml.PageMLElements.TABLE;
import static uk.ac.ebi.biostd.util.StringUtils.xmlEscaped;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.ebi.biostd.authz.AccessTag;
import uk.ac.ebi.biostd.in.PMDoc;
import uk.ac.ebi.biostd.in.pageml.PageMLAttributes;
import uk.ac.ebi.biostd.in.pageml.PageMLElements;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.model.AbstractAttribute;
import uk.ac.ebi.biostd.model.Annotated;
import uk.ac.ebi.biostd.model.FileRef;
import uk.ac.ebi.biostd.model.Link;
import uk.ac.ebi.biostd.model.Qualifier;
import uk.ac.ebi.biostd.model.Section;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.out.AbstractFormatter;

public class PageMLFormatter implements AbstractFormatter {

    public final static String dateFotmat = "yyyy-MM-dd";
    protected static final String shiftSym = " ";
    protected String initShift = shiftSym + shiftSym;
    private Appendable outStream;

    private DateFormat dateFmt;
    private boolean cutTech = false;

    public PageMLFormatter() {
    }

    public PageMLFormatter(Appendable o, boolean cut) {
        outStream = o;
        cutTech = cut;
    }


    @Override
    public void format(PMDoc document) throws IOException {
        header(document.getHeaders(), outStream);

        for (SubmissionInfo s : document.getSubmissions()) {
            format(s.getSubmission(), outStream);
        }

        footer(outStream);
    }


    @Override
    public void header(Map<String, List<String>> hdrs, Appendable out) throws IOException {
        out.append("<").append(ROOT.getElementName()).append(">\n");

        if (hdrs != null) {
            for (Map.Entry<String, List<String>> me : hdrs.entrySet()) {
                for (String val : me.getValue()) {
                    out.append(shiftSym).append("<").append(HEADER.getElementName()).append(">\n");

                    out.append(shiftSym).append(shiftSym).append("<").append(PageMLElements.NAME.getElementName())
                            .append(">");
                    xmlEscaped(me.getKey(), out);
                    out.append("</").append(PageMLElements.NAME.getElementName()).append(">\n");

                    out.append(shiftSym).append(shiftSym).append("<").append(PageMLElements.VALUE.getElementName())
                            .append(">");
                    xmlEscaped(val, out);
                    out.append("</").append(PageMLElements.VALUE.getElementName()).append(">\n");

                    out.append(shiftSym).append("</").append(HEADER.getElementName()).append(">\n");

                }
            }
        }

        out.append(shiftSym).append("<").append(SUBMISSIONS.getElementName()).append(">\n");

    }

    @Override
    public void footer(Appendable out) throws IOException {
        out.append(shiftSym).append("</").append(SUBMISSIONS.getElementName()).append(">\n");
        out.append("</").append(ROOT.getElementName()).append(">\n");
    }

    @Override
    public void separator(Appendable out) throws IOException {
    }

    @Override
    public void comment(String comment, Appendable out) throws IOException {
        out.append("<!-- ");
        xmlEscaped(comment);
        out.append(" -->\n");
    }


    @Override
    public void format(Submission s, Appendable out) throws IOException {
        formatSubmission(s, out, initShift);
    }

    protected void formatSubmission(Submission subm, Appendable out, String shift) throws IOException {
        out.append(shift);
        out.append('<').append(SUBMISSION.getElementName()).append(' ').append(ACCNO.getAttrName()).append("=\"");
        xmlEscaped(subm.getAccNo(), out);

        if (!cutTech) {
            out.append("\" ").append(ID.getAttrName()).append("=\"").append(String.valueOf(subm.getId()));
        }

        String str = subm.getEntityClass();
        if (str != null && str.length() > 0) {
            out.append("\" ").append(CLASS.getAttrName()).append("=\"");
            xmlEscaped(str, out);
        }

        str = subm.getRelPath();
        if (str != null && str.length() > 0 && !cutTech) {
            out.append("\" ").append(RELPATH.getAttrName()).append("=\"");
            xmlEscaped(str, out);
        }

        if (subm.getOwner() != null || (subm.getAccessTags() != null && subm.getAccessTags().size() > 0)) {
            out.append("\" ").append(ACCESS.getAttrName()).append("=\"");

            boolean needSep = false;

            if (!cutTech && subm.getOwner() != null) {
                out.append('~');

                String txtId = subm.getOwner().getEmail();

                if (txtId == null) {
                    txtId = subm.getOwner().getLogin();
                }

                xmlEscaped(txtId, out);

                out.append(";#");
                out.append(String.valueOf(subm.getOwner().getId()));
                needSep = true;
            }

            if (subm.getAccessTags() != null) {
                for (AccessTag at : subm.getAccessTags()) {
                    if (needSep) {
                        out.append(';');
                    } else {
                        needSep = true;
                    }

                    xmlEscaped(at.getName(), out);
                }
            }
        }

        if (!cutTech) {
            out.append("\" ").append(PageMLAttributes.CTIME.getAttrName()).append("=\"")
                    .append(String.valueOf(subm.getCTime()));
            out.append("\" ").append(PageMLAttributes.MTIME.getAttrName()).append("=\"")
                    .append(String.valueOf(subm.getMTime()));

            if (subm.isRTimeSet()) {
                out.append("\" ").append(PageMLAttributes.RTIME.getAttrName()).append("=\"")
                        .append(String.valueOf(subm.getRTime()));
            }

            if (subm.getSecretKey() != null) {
                out.append("\" ").append(PageMLAttributes.SECKEY.getAttrName()).append("=\"");
                xmlEscaped(subm.getSecretKey(), out);
            }
        }

        out.append("\">\n");

        String contShift = shift + shiftSym;

        Map<String, String> auxAttrMap = new HashMap<>();

        if (subm.getTitle() != null) {
            auxAttrMap.put(Submission.canonicTitleAttribute, subm.getTitle());
        }

        if (subm.isRTimeSet()) {
            if (dateFmt == null) {
                dateFmt = new SimpleDateFormat(dateFotmat);
            }

            auxAttrMap.put(Submission.canonicReleaseDateAttribute, dateFmt.format(new Date(subm.getRTime() * 1000)));
        }

        if (subm.getRootPath() != null && !cutTech) {
            auxAttrMap.put(Submission.canonicRootPathAttribute, subm.getRootPath());
        }

        formatAttributes(subm, auxAttrMap, out, contShift);

        out.append("\n");

        if (subm.getRootSection() != null) {
            formatSection(subm.getRootSection(), out, contShift);
        }

        out.append("\n");
        out.append(shift);
        out.append("</").append(SUBMISSION.getElementName()).append(">\n");
    }

    protected void formatSection(Section sec, Appendable out, String shift) throws IOException {

        out.append(shift);
        out.append('<').append(SECTION.getElementName());

        if (!cutTech) {
            out.append(' ').append(ID.getAttrName()).append("=\"").append(String.valueOf(sec.getId())).append("\"");
        }

        out.append(" ").append(TYPE.getAttrName()).append("=\"");
        xmlEscaped(sec.getType(), out);

        if (sec.getAccNo() != null) {
            out.append("\" ").append(ACCNO.getAttrName()).append("=\"");
            xmlEscaped(sec.getAccNo(), out);

            out.append("\" ").append(ACCNOGLOBAL.getAttrName()).append("=\"");
            out.append("false");
        }

        String str = sec.getEntityClass();
        if (str != null && str.length() > 0) {
            out.append("\" ").append(CLASS.getAttrName()).append("=\"");
            xmlEscaped(str, out);
        }

        if (sec.getAccessTags() != null && sec.getAccessTags().size() > 0) {
            out.append("\" ").append(ACCESS.getAttrName()).append("=\"");

            boolean first = true;
            for (AccessTag at : sec.getAccessTags()) {
                if (first) {
                    first = false;
                } else {
                    out.append(',');
                }

                xmlEscaped(at.getName(), out);
            }

        }

        out.append("\">\n\n");

        String contShift = shift + shiftSym;

        formatAttributes(sec, out, contShift);

        if (sec.getFileRefs() != null && sec.getFileRefs().size() > 0) {
            formatFileRefs(sec, out, contShift);
            out.append("\n");
        }

        if (sec.getLinks() != null && sec.getLinks().size() > 0) {
            formatLinks(sec, out, contShift);
            out.append("\n");
        }

        if (sec.getSections() != null && sec.getSections().size() > 0) {
            formatSubsections(sec.getSections(), out, contShift);
        }

        out.append("\n");
        out.append(shift);
        out.append("</").append(SECTION.getElementName()).append(">\n\n");

    }

    private void formatSubsections(List<Section> lst, Appendable out, String shift) throws IOException {
        String contShift = shift + shiftSym;

        out.append(shift);
        out.append("<").append(SUBSECTIONS.getElementName()).append(">\n");

        int lastTblIdx = -1;

        boolean hasTable = false;

        for (Section ssec : lst) {
            if (ssec.getTableIndex() <= lastTblIdx && hasTable) {
                contShift = shift + shiftSym;

                out.append(contShift);
                out.append("</").append(TABLE.getElementName()).append(">\n");

                hasTable = false;
            }

            if (ssec.getTableIndex() >= 0) {
                if (!hasTable) {
                    out.append(contShift);
                    out.append("<").append(TABLE.getElementName()).append(">\n");

                    contShift = contShift + shiftSym;
                    hasTable = true;
                }
            }

            lastTblIdx = ssec.getTableIndex();

            formatSection(ssec, out, contShift);
        }

        if (hasTable) {
            out.append(shift + shiftSym);
            out.append("</").append(TABLE.getElementName()).append(">\n");
        }

        out.append(shift);
        out.append("</").append(SUBSECTIONS.getElementName()).append(">\n");
    }

    private void formatLinks(Section s, Appendable out, String shift) throws IOException {

        out.append(shift);
        out.append("<").append(LINKS.getElementName()).append(">\n");

        String contShift = shift + shiftSym;

        int lastTblIdx = -1;

        boolean hasTable = false;

        for (Link ln : s.getLinks()) {
            if (ln.getTableIndex() <= lastTblIdx && hasTable) {
                contShift = shift + shiftSym;

                out.append(contShift);
                out.append("</").append(TABLE.getElementName()).append(">\n");

                hasTable = false;
            }

            if (ln.getTableIndex() >= 0) {
                if (!hasTable) {
                    out.append(contShift);
                    out.append("<").append(TABLE.getElementName()).append(">\n");

                    contShift = contShift + shiftSym;
                    hasTable = true;
                }
            }

            lastTblIdx = ln.getTableIndex();

            out.append(contShift);

            out.append('<').append(LINK.getElementName());

//   out.append('<').append(LINK.getElementName()).append(' ').append(URL.getAttrName()).append("=\"");
//   xmlEscaped(ln.getUrl(),out);

            String str = ln.getEntityClass();
            if (str != null && str.length() > 0) {
                out.append(" ").append(CLASS.getAttrName()).append("=\"");
                xmlEscaped(str, out);
                out.append("\"");
            }

            if (ln.getAccessTags() != null && ln.getAccessTags().size() > 0) {
                out.append(" ").append(ACCESS.getAttrName()).append("=\"");

                boolean first = true;
                for (AccessTag at : ln.getAccessTags()) {
                    if (first) {
                        first = false;
                    } else {
                        out.append(';');
                    }

                    xmlEscaped(at.getName(), out);
                }

                out.append("\"");

            }

            out.append(">\n");

            out.append(contShift + shiftSym).append('<').append(PageMLElements.URL.getElementName()).append(">");
            xmlEscaped(ln.getUrl(), out);
            out.append("</").append(PageMLElements.URL.getElementName()).append(">\n");

            formatAttributes(ln, out, contShift + shiftSym);

            out.append(contShift);
            out.append("</").append(LINK.getElementName()).append(">\n");

        }

        if (hasTable) {
            out.append(shift + shiftSym);
            out.append("</").append(TABLE.getElementName()).append(">\n");
        }

        out.append(shift);
        out.append("</").append(LINKS.getElementName()).append(">\n");

    }

    private void formatFileRefs(Section s, Appendable out, String shift) throws IOException {
        out.append(shift);
        out.append("<").append(FILES.getElementName()).append(">\n");

        String contShift = shift + shiftSym;

        int lastTblIdx = -1;

        boolean hasTable = false;

        for (FileRef fr : s.getFileRefs()) {
            if (fr.getTableIndex() <= lastTblIdx && hasTable) {
                contShift = shift + shiftSym;

                out.append(contShift);
                out.append("</").append(TABLE.getElementName()).append(">\n");

                hasTable = false;
            }

            if (fr.getTableIndex() >= 0) {
                if (!hasTable) {
                    out.append(contShift);
                    out.append("<").append(TABLE.getElementName()).append(">\n");

                    contShift = contShift + shiftSym;
                    hasTable = true;
                }
            }

            lastTblIdx = fr.getTableIndex();

            out.append(contShift);

            out.append('<').append(FILE.getElementName());

            out.append(' ').append(SIZE.getAttrName()).append("=\"").append(String.valueOf(fr.getSize()))
                    .append("\" type=\"");

            if (fr.isDirectory()) {
                out.append("directory");
            } else {
                out.append("file");
            }

            out.append("\"");

            String str = fr.getEntityClass();
            if (str != null && str.length() > 0) {
                out.append(" ").append(CLASS.getAttrName()).append("=\"");
                xmlEscaped(str, out);
                out.append("\"");
            }

            if (fr.getAccessTags() != null && fr.getAccessTags().size() > 0) {
                out.append(" ").append(ACCESS.getAttrName()).append("=\"");

                boolean first = true;
                for (AccessTag at : fr.getAccessTags()) {
                    if (first) {
                        first = false;
                    } else {
                        out.append(';');
                    }

                    xmlEscaped(at.getName(), out);
                }

                out.append("\"");

            }

            out.append(">\n");

            out.append(contShift + shiftSym).append('<').append(PageMLElements.PATH.getElementName()).append(">");

            if (fr.getPath() != null) {
                xmlEscaped(fr.getPath(), out);
            } else {
                xmlEscaped(fr.getName(), out);
            }

            out.append("</").append(PageMLElements.PATH.getElementName()).append(">\n");

            formatAttributes(fr, out, contShift + shiftSym);

            out.append(contShift);
            out.append("</").append(FILE.getElementName()).append(">\n");

        }

        if (hasTable) {
            out.append(shift + shiftSym);
            out.append("</").append(TABLE.getElementName()).append(">\n");
        }

        out.append(shift);
        out.append("</").append(FILES.getElementName()).append(">\n");
    }

    protected void formatAttributes(Annotated ent, Appendable out, String shift) throws IOException {
        formatAttributes(ent, null, out, shift);
    }

    protected void formatAttributes(Annotated ent, Map<String, String> aux, Appendable out, String shift)
            throws IOException {

        out.append(shift);
        out.append("<").append(ATTRIBUTES.getElementName());

        String atShift = shift + shiftSym;

        List<? extends AbstractAttribute> attrs = ent.getAttributes();

        if ((attrs == null || attrs.size() == 0) && (aux == null || aux.size() == 0)) {
            out.append("/>\n");
            return;
        }

        out.append(">\n");

        if (aux != null) {
            for (Map.Entry<String, String> me : aux.entrySet()) {
                out.append(atShift);
                out.append('<').append(ATTRIBUTE.getElementName()).append(">\n");

                String vshift = atShift + shiftSym;

                out.append(vshift).append('<').append(PageMLElements.NAME.getElementName()).append('>');
                xmlEscaped(me.getKey(), out);
                out.append("</").append(PageMLElements.NAME.getElementName()).append(">\n");

                out.append(vshift).append('<').append(PageMLElements.VALUE.getElementName()).append('>');
                xmlEscaped(me.getValue(), out);
                out.append("</").append(PageMLElements.VALUE.getElementName()).append(">\n");

                out.append(atShift);
                out.append("</").append(ATTRIBUTE.getElementName()).append(">\n");
            }
        }

        for (AbstractAttribute at : attrs) {
            out.append(atShift);
            out.append('<').append(ATTRIBUTE.getElementName());

            if (at.isReference()) {
                out.append(" reference=\"true\"");
            }

            String str = at.getEntityClass();
            if (str != null && str.length() > 0) {
                out.append(" ").append(CLASS.getAttrName()).append("=\"");
                xmlEscaped(str, out);
                out.append("\"");
            }

            out.append(">\n");

            String vshift = atShift + shiftSym;

            out.append(vshift).append('<').append(PageMLElements.NAME.getElementName()).append('>');
            xmlEscaped(at.getName(), out);
            out.append("</").append(PageMLElements.NAME.getElementName()).append(">\n");

            List<Qualifier> qlist = at.getNameQualifiers();

            if (qlist != null && qlist.size() > 0) {
                for (Qualifier q : qlist) {
                    formatQualifier(q, PageMLElements.NMQUALIFIER.getElementName(), out, vshift);
                }
            }

            out.append(vshift).append('<').append(PageMLElements.VALUE.getElementName()).append('>');
            xmlEscaped(at.getValue(), out);
            out.append("</").append(PageMLElements.VALUE.getElementName()).append(">\n");

            qlist = at.getValueQualifiers();

            if (qlist != null && qlist.size() > 0) {
                for (Qualifier q : qlist) {
                    formatQualifier(q, PageMLElements.VALQUALIFIER.getElementName(), out, vshift);
                }
            }

            out.append(atShift);
            out.append("</").append(ATTRIBUTE.getElementName()).append(">\n");

        }

        out.append(shift);
        out.append("</").append(ATTRIBUTES.getElementName()).append(">\n");

    }

    private void formatQualifier(Qualifier q, String xmltag, Appendable out, String shift) throws IOException {
        out.append(shift).append('<').append(xmltag).append(">\n");

        String vshift = shift + shiftSym;

        out.append(vshift).append('<').append(PageMLElements.NAME.getElementName()).append('>');
        xmlEscaped(q.getName(), out);
        out.append("</").append(PageMLElements.NAME.getElementName()).append(">\n");

        out.append(vshift).append('<').append(PageMLElements.VALUE.getElementName()).append('>');
        xmlEscaped(q.getValue(), out);
        out.append("</").append(PageMLElements.VALUE.getElementName()).append(">\n");

        out.append(shift).append("</").append(xmltag).append(">\n");
    }


}
