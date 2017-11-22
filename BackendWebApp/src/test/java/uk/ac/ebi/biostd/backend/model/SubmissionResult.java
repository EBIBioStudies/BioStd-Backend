package uk.ac.ebi.biostd.backend.model;


import java.util.List;
import lombok.Data;

@Data
public class SubmissionResult {

    private String status;
    private List<Mapping> mapping;
    private Log log;
}
