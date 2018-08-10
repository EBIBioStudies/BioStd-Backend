package uk.ac.ebi.biostd.webapp.application.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class FileDto {
    private String name;
    private String path;
    private FileType Type;
    private long size;
    private List<FileDto> files;
}
