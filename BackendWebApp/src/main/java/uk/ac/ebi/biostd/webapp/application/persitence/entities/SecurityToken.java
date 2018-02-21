package uk.ac.ebi.biostd.webapp.application.persitence.entities;

import java.time.OffsetDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity
public class SecurityToken {

    @Id
    private String id;
    private OffsetDateTime invalidationDate;
}
