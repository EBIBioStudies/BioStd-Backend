package uk.ac.ebi.biostd.backend.model;

import java.util.List;
import lombok.Data;

@Data
public class Mapping {

    private List<String> sections;
    private String order;
    private String assigned;
    private String original;
}
