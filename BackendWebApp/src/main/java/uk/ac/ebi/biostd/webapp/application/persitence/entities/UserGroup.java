package uk.ac.ebi.biostd.webapp.application.persitence.entities;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "UserGroup")
@NoArgsConstructor
public class UserGroup {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;
    private String description;
    private String name;
    private boolean project;
    private String secret;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToMany(cascade = {PERSIST, MERGE})
    @JoinTable(name = "UserGroup_User",
            joinColumns = @JoinColumn(name = "groups_id"),
            inverseJoinColumns = @JoinColumn(name = "users_id"))
    private Set<User> users = new HashSet<>();
}
