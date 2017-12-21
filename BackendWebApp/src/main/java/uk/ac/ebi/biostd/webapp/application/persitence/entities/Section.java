package uk.ac.ebi.biostd.webapp.application.persitence.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Section")
public class Section {

    @Id
    @GeneratedValue
    private long id;

    @Column(name = "type")
    private String type;
}
