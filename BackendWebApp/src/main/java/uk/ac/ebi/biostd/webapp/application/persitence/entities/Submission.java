package uk.ac.ebi.biostd.webapp.application.persitence.entities;

import static javax.persistence.FetchType.LAZY;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Submission {

    @GeneratedValue
    @Id
    private long id;

    @Column(name = "cTime")
    private long cTime;

    @Column(name = "MTime")
    private long mTime;

    @Column(name = "RTime")
    private long rTime;

    @Column(name = "accNo")
    private String accNo;

    @Column(name = "relPath")
    private String relativePath;

    @Column(name = "released")
    public boolean released;

    @Column(name = "rootPath")
    private String rootPath;

    @Column(name = "title")
    private String title;

    @Column(name = "version")
    private int version;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "rootSection_id")
    private Section rootSection;

    @ManyToOne()
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "owner_id", updatable = false, insertable = false)
    private long ownerId;

    @ManyToMany
    @JoinTable(name = "Submission_AccessTag",
            joinColumns = @JoinColumn(name = "Submission_Id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "accessTags_id", referencedColumnName = "id"))
    private Set<AccessTag> accessTag;
}
