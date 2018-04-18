package uk.ac.ebi.biostd.webapp.application.persitence.entities;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.GenerationType.IDENTITY;

import java.util.Set;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.AbstractAggregateRoot;
import uk.ac.ebi.biostd.webapp.application.domain.events.PassResetEvent;
import uk.ac.ebi.biostd.webapp.application.domain.events.UserCreatedEvent;
import uk.ac.ebi.biostd.webapp.application.persitence.common.AuxInfo;
import uk.ac.ebi.biostd.webapp.application.persitence.common.UserAuxInfoConverter;

@Entity
@Getter
@Setter
@Table(name = "User")
@NoArgsConstructor
public class User extends AbstractAggregateRoot {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;

    private String email;
    private String fullName;
    private String login;
    private boolean superuser;
    private boolean active;
    private long keyTime;

    @Convert(converter = UserAuxInfoConverter.class)
    private AuxInfo auxProfileInfo;
    private String activationKey;

    @Lob
    private byte[] passwordDigest;

    @OneToMany(mappedBy = "user", cascade = {PERSIST, MERGE})
    private Set<AccessPermission> accessPermissions;

    public User withPendingActivation(String activationLink) {
        registerEvent(new UserCreatedEvent(this, activationLink));
        return this;
    }

    public User withResetPasswordRequest(String activationLink) {
        registerEvent(new PassResetEvent(this, activationLink));
        return this;
    }
}
