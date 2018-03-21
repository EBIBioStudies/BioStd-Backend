package uk.ac.ebi.biostd.webapp.application.persitence.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class UserDataId implements Serializable {

    @Column(name = "dataKey")
    private String dataKey;

    @Column(name = "userId")
    private Long userId;
}
