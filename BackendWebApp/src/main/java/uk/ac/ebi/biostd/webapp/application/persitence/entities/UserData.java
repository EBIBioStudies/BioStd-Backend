package uk.ac.ebi.biostd.webapp.application.persitence.entities;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class UserData {

    @EmbeddedId
    private UserDataId userDataId;

    @Column(name = "data")
    private String data;

    @Column(name = "topic")
    private String topic;

    @Column(name = "contentType")
    private String contentType;

    public UserData(UserDataId userDataId) {
        this.userDataId = userDataId;
    }
}
