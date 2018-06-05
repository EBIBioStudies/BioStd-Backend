package uk.ac.ebi.biostd.webapp.application.security.rest.exceptions;

import static java.lang.String.format;

import uk.ac.ebi.biostd.webapp.application.persitence.entities.User;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(User user) {
        super(format("there is already a user register with email %s", user.getEmail()));
    }
}
