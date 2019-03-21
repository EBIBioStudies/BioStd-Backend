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
import java.util.Collections;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import uk.ac.ebi.biostd.authz.AccessTag;
import uk.ac.ebi.biostd.authz.Tag;
import uk.ac.ebi.biostd.authz.TagRef;

@Entity
@NamedQueries({
        @NamedQuery(name = "Section.countByAcc",
                query = "SELECT count(s) FROM Section s where s.accNo=:accNo"),
        @NamedQuery(name = "Section.countByAccActive",
                query = "SELECT count(s) FROM Section s JOIN s.submission sbm where sbm.version > 0 AND s"
                        + ".accNo=:accNo")
})
@Table(indexes = {
        @Index(name = "acc_idx", columnList = "accNo"),
        @Index(name = "section_type_index", columnList = "type")
})
public class Section implements Node, Accessible {

    private long id;
    private String parentAcc;
    private List<SectionAttribute> attributes;
    private Submission submission;
    private Section parentNode;
    private String type;
    private String acc;
    private List<Section> sections;
    private List<FileRef> fileRefs;
    private List<Link> links;
    private Collection<SectionTagRef> tagRefs;
    private Collection<AccessTag> accessTags;
    private int tableIndex = -1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getParentAccNo() {
        return parentAcc;
    }

    public void setParentAccNo(String pa) {
        parentAcc = pa;
    }

    @Override
    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    @OrderColumn(name = "ord", insertable = true)
    public List<SectionAttribute> getAttributes() {
        if (attributes == null) {
            return Collections.emptyList();
        }

        return attributes;
    }

    public void setAttributes(List<SectionAttribute> sn) {
        attributes = sn;

        if (sn == null) {
            return;
        }

        for (SectionAttribute sa : sn) {
            sa.setHost(this);
        }
    }

    public void addAttribute(SectionAttribute nd) {
        if (attributes == null) {
            attributes = new ArrayList<>();
        }

        attributes.add(nd);
        nd.setHost(this);
    }

    @Override
    public boolean removeAttribute(AbstractAttribute at) {
        if (attributes == null) {
            return false;
        }

        return attributes.remove(at);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission study) {
        submission = study;

        if (sections != null) {
            for (Section s : sections) {
                s.setSubmission(study);
            }
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    public Section getParentSection() {
        return parentNode;
    }

    public void setParentSection(Section pr) {
        parentNode = pr;

        if (pr == null) {
            return;
        }

        parentAcc = pr.getAccNo();

        setSubmission(pr.getSubmission());

        if (sections != null) {
            for (Section s : sections) {
                s.setParentSection(this);
            }
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getAccNo() {
        return acc;
    }

    @Override
    public void setAccNo(String acc) {
        this.acc = acc;

        if (sections != null) {
            for (Section s : sections) {
                s.setParentAccNo(acc);
            }
        }
    }

    @OneToMany(mappedBy = "parentSection", cascade = CascadeType.ALL)
    @OrderColumn(name = "ord")
    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sn) {
        sections = sn;

        if (sn == null) {
            return;
        }

        for (Section s : sn) {
            s.setParentSection(this);
        }
    }

    public void addSection(Section nd) {
        if (sections == null) {
            sections = new ArrayList<>();
        }

        sections.add(nd);

        nd.setParentSection(this);
    }

    @OneToMany(mappedBy = "hostSection", cascade = CascadeType.ALL)
    @OrderColumn(name = "ord")
    public List<FileRef> getFileRefs() {
        return fileRefs;
    }

    public void setFileRefs(List<FileRef> sn) {
        fileRefs = sn;

        if (sn == null) {
            return;
        }

        for (FileRef s : sn) {
            s.setHostSection(this);
        }

    }

    public void addFileRef(FileRef nd) {
        if (fileRefs == null) {
            fileRefs = new ArrayList<>();
        }

        fileRefs.add(nd);

        nd.setHostSection(this);
    }

    @OneToMany(mappedBy = "hostSection", cascade = CascadeType.ALL)
    @OrderColumn(name = "ord")
    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> sn) {
        links = sn;

        if (sn == null) {
            return;
        }

        for (Link s : sn) {
            s.setHostSection(this);
        }

    }

    public void addLink(Link nd) {
        if (links == null) {
            links = new ArrayList<>();
        }

        links.add(nd);
        nd.setHostSection(this);
    }

    @Override
    public AbstractAttribute addAttribute(String name, String value) {
        SectionAttribute sa = new SectionAttribute(name, value);

        addAttribute(sa);

        return sa;
    }

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
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
    public Collection<SectionTagRef> getTagRefs() {
        return tagRefs;
    }

    public void setTagRefs(Collection<SectionTagRef> tags) {
        tagRefs = tags;

        if (tags != null) {
            for (SectionTagRef str : tags) {
                str.setSection(this);
            }
        }
    }

    @Override
    public SectionTagRef addTagRef(Tag t, String val) {
        SectionTagRef ftr = new SectionTagRef();

        ftr.setTag(t);
        ftr.setParameter(val);

        addTagRef(ftr);

        return ftr;
    }

    public void addTagRef(SectionTagRef tr) {
        if (tagRefs == null) {
            tagRefs = new ArrayList<>();
        }

        tr.setSection(this);

        tagRefs.add(tr);
    }

    @Override
    @ManyToMany
    public Collection<AccessTag> getAccessTags() {
        return accessTags;
    }

    public void setAccessTags(Collection<AccessTag> accessTags) {
        this.accessTags = accessTags;
    }

    @Override
    public void addAccessTag(AccessTag t) {
        if (accessTags == null) {
            accessTags = new ArrayList<>();
        }

        accessTags.add(t);
    }

    public int getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(int tableIndex) {
        this.tableIndex = tableIndex;
    }

}
