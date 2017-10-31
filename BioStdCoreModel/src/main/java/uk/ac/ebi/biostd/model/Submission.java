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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import uk.ac.ebi.biostd.authz.AccessTag;
import uk.ac.ebi.biostd.authz.OwnedObject;
import uk.ac.ebi.biostd.authz.Tag;
import uk.ac.ebi.biostd.authz.TagRef;
import uk.ac.ebi.biostd.authz.User;


@Entity
@NamedQueries({@NamedQuery(name = Submission.GetCountByAccQuery,
        query = "SELECT count(s) FROM Submission s where s.accNo=:accNo AND s.version > 0"),
        @NamedQuery(name = Submission.GetCountAllByAccQuery,
                query = "SELECT count(s) FROM Submission s where s.accNo=:accNo"),
        @NamedQuery(name = Submission.GetByAccQuery,
                query = "SELECT s FROM Submission s where s.accNo=:accNo AND s.version > 0"),
        @NamedQuery(name = Submission.GetAllByAccQuery, query = "SELECT s FROM Submission s where s.accNo=:accNo"),
        @NamedQuery(name = Submission.GetByOwnerQuery,
                query = "SELECT s from Submission s JOIN s.owner u where u.id=:uid AND s.version > 0 order by s.MTime"
                        + " desc"), @NamedQuery(name = Submission.GetAccByPatQuery,
        query = "SELECT s.accNo FROM Submission s where s.accNo LIKE :pattern"),
        @NamedQuery(name = Submission.GetByIdQuery, query = "SELECT s FROM Submission s where s.id=:id"),
        @NamedQuery(name = Submission.GetMinVer, query = "SELECT MIN(s.version) FROM Submission s where s.accNo=:accNo")

})
@Table(indexes = {@Index(name = "rtime_idx", columnList = "RTime"),
        @Index(name = "released_idx", columnList = "released")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"accNo", "version"})})
public class Submission implements Node, Accessible, OwnedObject {

    public static final String GetCountByAccQuery = "Submission.countByAcc";
    public static final String GetCountAllByAccQuery = "Submission.counAlltByAcc";
    public static final String GetByAccQuery = "Submission.getByAcc";
    public static final String GetByIdQuery = "Submission.getById";
    public static final String GetAllByAccQuery = "Submission.getAllByAcc";
    public static final String GetByOwnerQuery = "Submission.getByOwner";
    public static final String GetAccByPatQuery = "Submission.getAccByPat";
    public static final String GetMinVer = "Submission.getMinVer";

    public static final String canonicReleaseDateAttribute = "ReleaseDate";
    public static final String canonicTitleAttribute = "Title";
    public static final String canonicRootPathAttribute = "RootPath";
    public static final String canonicAttachToAttribute = "AttachTo";
    public static final String canonicAccNoPattern = "AccNoPattern";
    public static final String canonicAccNoTemplate = "AccNoTemplate";

    private static final Pattern releaseDateAttribute = Pattern.compile("Release\\s*Date", Pattern.CASE_INSENSITIVE);
    private static final Pattern titleAttribute = Pattern.compile("Title", Pattern.CASE_INSENSITIVE);
    private static final Pattern rootPathAttribute = Pattern.compile("Root\\s*Path", Pattern.CASE_INSENSITIVE);
    private static final Pattern attachToAttribute = Pattern.compile("Attach\\s*To", Pattern.CASE_INSENSITIVE);
    private static final Pattern accNoPatternAttribute = Pattern
            .compile("Acc\\s*No\\s*Pattern", Pattern.CASE_INSENSITIVE);
    private static final Pattern accNoTemplateAttribute = Pattern
            .compile("Acc\\s*No\\s*Template", Pattern.CASE_INSENSITIVE);

    private static final Pattern releaseDateFormat = Pattern.compile(
            "(?<year>\\d{2,4})-(?<month>\\d{1,2})-(?<day>\\d{1,2})(T(?<hour>\\d{1,2}):(?<min>\\d{1,2})(:(?<sec>\\d{1,"
                    + "2})(\\.(?<msec>\\d{1,3})Z?)?)?)?");
    private long id;
    private String acc;
    private int ver;
    private String rootPath;
    private String relPath;
    private long ctime;
    private long mtime;
    private long rtime = -1;
    private boolean released;
    private String title;
    private User owner;
    private List<SubmissionAttribute> attributes;
    private Section rootSection;
    private Collection<SubmissionTagRef> tagRefs;
    private Collection<AccessTag> accessTags;
    private String secretKey;

    private static String getAttribute(Node nd, Pattern atpat) {
        if (nd.getAttributes() != null) {
            Matcher mtch = atpat.matcher("");

            for (AbstractAttribute attr : nd.getAttributes()) {
                mtch.reset(attr.getName());

                if (mtch.matches()) {
                    return attr.getValue();
                }
            }
        }

        return null;
    }

    public static String getNodeTitle(Node nd) {
        return getAttribute(nd, titleAttribute);
    }

    public static String getNodeReleaseDate(Node nd) {
        return getAttribute(nd, releaseDateAttribute);
    }

    public static String getNodeAccNoPattern(Node nd) {
        return getAttribute(nd, accNoPatternAttribute);
    }

    public static String getNodeAccNoTemplate(Node nd) {
        return getAttribute(nd, accNoTemplateAttribute);
    }

    public static List<String> getNodeAttachTo(Node nd) {
        if (nd.getAttributes() != null) {
            List<String> res = null;

            Matcher mtch = attachToAttribute.matcher("");

            for (AbstractAttribute attr : nd.getAttributes()) {
                mtch.reset(attr.getName());

                if (mtch.matches()) {
                    if (res == null) {
                        res = new ArrayList<>();
                    }

                    String val = attr.getValue();

                    if (val != null && (val = val.trim()).length() > 0) {
                        res.add(val);
                    }
                }
            }

            return res;
        }

        return null;
    }

    public static String getNodeRootPath(Node nd) {
        return getAttribute(nd, rootPathAttribute);
    }

    public static long readReleaseDate(String val) {
        val = val.trim();

        if (val.length() > 0) {
            Matcher rdValMatcher = releaseDateFormat.matcher(val);

            if (rdValMatcher.matches()) {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

                cal.set(Calendar.YEAR, Integer.parseInt(rdValMatcher.group("year")));
                cal.set(Calendar.MONTH, Integer.parseInt(rdValMatcher.group("month")) - 1);
                cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(rdValMatcher.group("day")));

                String str = rdValMatcher.group("hour");

                if (str != null) {
                    cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(str));
                }

                str = rdValMatcher.group("min");

                if (str != null) {
                    cal.set(Calendar.MINUTE, Integer.parseInt(str));
                }

                str = rdValMatcher.group("sec");

                if (str != null) {
                    cal.set(Calendar.SECOND, Integer.parseInt(str));
                }

                return cal.getTimeInMillis();

            }
        }

        return -1;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getAccNo() {
        return acc;
    }

    @Override
    public void setAccNo(String acc) {
        this.acc = acc;
    }

    public int getVersion() {
        return ver;
    }

    public void setVersion(int v) {
        ver = v;
    }

    @Lob
    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @Lob
    public String getRelPath() {
        return relPath;
    }

    public void setRelPath(String rp) {
        relPath = rp;
    }

    public long getCTime() {
        return ctime;
    }

    public void setCTime(long tm) {
        ctime = tm;
    }

    public long getMTime() {
        return mtime;
    }

    public void setMTime(long tm) {
        mtime = tm;
    }

    public long getRTime() {
        return rtime;
    }

    public void setRTime(long tm) {
        rtime = tm;
    }

    @Transient
    public boolean isRTimeSet() {
        return rtime >= 0;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean rls) {
        released = rls;
    }

    @Lob
    public String getTitle() {
        return title;
    }

    public void setTitle(String tl) {
        title = tl;
    }

    public String createTitle() {
        String ttl = getTitle();

        if (ttl != null) {
            return ttl;
        }

        if (getAttributes() != null) {
            Matcher mtch = titleAttribute.matcher("");

            for (SubmissionAttribute attr : getAttributes()) {
                mtch.reset(attr.getName());

                if (mtch.matches()) {
                    ttl = attr.getValue();
                    break;
                }
            }
        }

        if (ttl == null) {
            ttl = getAccNo() + " " + SimpleDateFormat.getDateTimeInstance().format(new Date(getMTime() * 1000L));
        }

        return ttl;
    }

    @Override
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @Override
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "host", cascade = CascadeType.ALL)
    @OrderColumn(name = "ord")
    public List<SubmissionAttribute> getAttributes() {
        if (attributes == null) {
            return Collections.emptyList();
        }

        return attributes;
    }

    public void setAttributes(List<SubmissionAttribute> sn) {
        attributes = sn;

        if (sn == null) {
            return;
        }

        for (SubmissionAttribute sa : sn) {
            sa.setHost(this);
        }
    }

    public void addAttribute(SubmissionAttribute nd) {
        if (attributes == null) {
            attributes = new ArrayList<>();
        }

        attributes.add(nd);
        nd.setHost(this);
    }

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @NotNull
    public Section getRootSection() {
        return rootSection;
    }

    public void setRootSection(Section rootSection) {
        this.rootSection = rootSection;

        rootSection.setSubmission(this);
    }

    @Override
    public AbstractAttribute addAttribute(String name, String value) {
        SubmissionAttribute sa = new SubmissionAttribute(name, value);

        addAttribute(sa);

        return sa;
    }

    @Override
    public boolean removeAttribute(AbstractAttribute at) {
        if (attributes == null) {
            return false;
        }

        return attributes.remove(at);
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
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "submission", cascade = CascadeType.ALL)
    public Collection<SubmissionTagRef> getTagRefs() {
        return tagRefs;
    }

    public void setTagRefs(Collection<SubmissionTagRef> tags) {
        tagRefs = tags;

        if (tags != null) {
            for (SubmissionTagRef str : tags) {
                str.setSubmission(this);
            }
        }
    }

    @Override
    public SubmissionTagRef addTagRef(Tag t, String val) {
        SubmissionTagRef ftr = new SubmissionTagRef();

        ftr.setTag(t);
        ftr.setParameter(val);

        addTagRef(ftr);

        return ftr;
    }

    public void addTagRef(SubmissionTagRef tr) {
        if (tagRefs == null) {
            tagRefs = new ArrayList<>();
        }

        tr.setSubmission(this);

        tagRefs.add(tr);
    }

    @Override
    @ManyToMany(fetch = FetchType.LAZY)
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

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void normalizeAttributes() throws SubmissionAttributeException {
        if (getAttributes() == null || getAttributes().size() == 0) {
            if (getRootSection() != null) {
                setTitle(getNodeTitle(getRootSection()));
            }

            return;
        }

        Iterator<SubmissionAttribute> saitr = getAttributes().iterator();

        boolean rTimeFound = false;
        boolean rootPathFound = false;

        String rootPathAttr = null;

        Matcher rdMtMatcher = releaseDateAttribute.matcher("");
        Matcher rootPMatcher = rootPathAttribute.matcher("");
        Matcher titleMatcher = titleAttribute.matcher("");

        while (saitr.hasNext()) {
            SubmissionAttribute sa = saitr.next();

            rdMtMatcher.reset(sa.getName());

            if (rdMtMatcher.matches()) {
                saitr.remove();

                if (rTimeFound) {
                    throw new SubmissionAttributeException(
                            "Multiple '" + sa.getName() + "' attributes are not allowed");
                }

                rTimeFound = true;

                String val = sa.getValue();

                if (val != null) {
                    long relDate = readReleaseDate(val);

                    if (relDate < 0) {
                        throw new SubmissionAttributeException("Invalid '" + sa.getName()
                                + "' attribute value. Expected date in format: YYYY-MM-DD[Thh:mm[:ss[.mmm]]]");
                    }

                    setRTime(relDate / 1000);
                }

            } else if (rootPMatcher.reset(sa.getName()) != null && rootPMatcher.matches()) {
                saitr.remove();

                if (rootPathFound) {
                    new SubmissionAttributeException("Multiple '" + sa.getName() + "' attributes are not allowed");
                }

                rootPathFound = true;

                rootPathAttr = sa.getValue();
                setRootPath(rootPathAttr);
            } else if (titleMatcher.reset(sa.getName()) != null && titleMatcher.matches()) {
                saitr.remove();

                setTitle(sa.getValue());
            }

        }

        if (getTitle() == null && getRootSection() != null) {
            setTitle(getNodeTitle(getRootSection()));
        }

    }


}
