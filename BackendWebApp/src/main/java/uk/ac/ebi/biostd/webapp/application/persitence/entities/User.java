package uk.ac.ebi.biostd.webapp.application.persitence.entities;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;

import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.AbstractAggregateRoot;
import uk.ac.ebi.biostd.webapp.application.domain.events.PassResetRequest;
import uk.ac.ebi.biostd.webapp.application.domain.events.UserCreatedEvent;

@Entity
@Getter
@Setter
@Table(name = "User")
@Builder
public class User extends AbstractAggregateRoot {

    @Id
    @GeneratedValue
    private long id;

    private String email;
    private String fullName;
    private String login;
    private boolean superuser;
    private boolean active;
    private String aux;
    private String activationKey;

    @Lob
    private byte[] passwordDigest;

    @OneToMany(mappedBy = "user", cascade = {PERSIST, MERGE})
    private Set<AccessPermission> accessPermissions;

    public User withPendingActivation(String activationLink) {
        registerEvent(new UserCreatedEvent(this, activationLink));
        return this;
    }

    public User withResetPassword(String activationLink) {
        registerEvent(new PassResetRequest(this, activationLink));
        return this;
    }
}
