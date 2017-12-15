package uk.ac.ebi.biostd.webapp.application.persitence.entities;

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
    @GeneratedValue
    private long id;

    private String name;
}
