package uk.ac.ebi.biostd.webapp.application.persitence.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue
    private long id;

    private String email;
    private String fullName;
    private String login;
    private boolean superuser;

    @Lob
    private byte[] passwordDigest;
}
