package uk.ac.ebi.biostd.webapp.application.domain.events;

import lombok.Data;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;

@Data
public class PassResetEvent {

    private final User user;
    private final String activationLink;
}
