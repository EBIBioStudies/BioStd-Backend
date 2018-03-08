package uk.ac.ebi.biostd.webapp.application.persitence.entities;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity
public class SecurityToken {

    @Id
    private String id;

    @Column(name = "invalidation_date")
    private OffsetDateTime invalidationDate;
}
