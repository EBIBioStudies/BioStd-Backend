package uk.ac.ebi.biostd.webapp.application.persitence.entities;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "AccessTag")
public class AccessTag {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;

    private String name;
}
