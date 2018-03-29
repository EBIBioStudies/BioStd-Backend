package uk.ac.ebi.biostd.remote.dto;

import java.util.List;
import lombok.Data;

@Data
public class LogDto {

    private String message;
    private List<Subnodes> subnodes;
    private String level;
}
