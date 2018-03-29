package uk.ac.ebi.biostd.remote.dto;

import java.util.List;
import lombok.Data;

@Data
public class MappingDto {

    private List<String> sections;
    private String order;
    private String assigned;
    private String original;
}
