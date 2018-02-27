package uk.ac.ebi.biostd.webapp.application.security.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;

@Data
@AllArgsConstructor
public class UserData {

    private String token;
    private User user;

}
