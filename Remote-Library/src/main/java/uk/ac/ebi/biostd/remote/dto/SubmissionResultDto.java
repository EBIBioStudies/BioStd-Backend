package uk.ac.ebi.biostd.remote.dto;


import java.util.List;
import lombok.Data;

@Data
public class SubmissionResultDto {

    private String status;
    private List<MappingDto> mapping;
    private LogDto log;
}
