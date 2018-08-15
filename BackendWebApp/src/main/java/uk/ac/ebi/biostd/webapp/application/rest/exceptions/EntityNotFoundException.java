package uk.ac.ebi.biostd.webapp.application.rest.exceptions;

import lombok.Getter;

public class EntityNotFoundException extends RuntimeException {
    @Getter
    private Class<?> entity;

    public EntityNotFoundException(String message, Class<?> entity) {
        super(message);
        this.entity = entity;
    }
}
