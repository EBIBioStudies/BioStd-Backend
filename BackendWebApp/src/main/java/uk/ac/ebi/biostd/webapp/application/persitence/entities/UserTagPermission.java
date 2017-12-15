package uk.ac.ebi.biostd.webapp.application.persitence.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DelegateProfUsrACR")
@Getter
@Setter
public class UserTagPermission {

    @GeneratedValue
    @Id
    private long id;

    @OneToOne
    @JoinColumn(name = "subject_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "host_id")
    private AccessTag accessTag;

    @OneToOne
    @JoinColumn(name = "profile_id")
    private Permission permission;
}
