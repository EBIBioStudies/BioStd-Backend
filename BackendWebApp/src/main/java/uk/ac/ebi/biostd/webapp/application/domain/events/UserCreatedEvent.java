package uk.ac.ebi.biostd.webapp.application.domain.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;

@AllArgsConstructor
@Getter
public class UserCreatedEvent {

    private final User user;
    private final String activationLink;
}
