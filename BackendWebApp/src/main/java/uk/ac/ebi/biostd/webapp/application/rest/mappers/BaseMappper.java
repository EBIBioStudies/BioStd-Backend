package uk.ac.ebi.biostd.webapp.application.rest.mappers;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseMappper<S, T> {

    public abstract S map(T t);

    public List<S> map(List<T> list) {
        return list.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}
