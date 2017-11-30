package uk.ac.ebi.biostd.backend.model;

import java.util.List;
import lombok.Data;

@Data
public class Log {

    private String message;
    private List<Subnodes> subnodes;
    private String level;
}
