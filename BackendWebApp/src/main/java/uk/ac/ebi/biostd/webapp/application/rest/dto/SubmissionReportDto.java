package uk.ac.ebi.biostd.webapp.application.rest.dto;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.biostd.treelog.SubmissionReport;

@Data
@Builder
public class SubmissionReportDto {
    private String status;
    private List<SubmissionMappingDto> mapping;
    private LogNodeDto log;

    public static SubmissionReportDto from(SubmissionReport report) {
        return SubmissionReportDto.builder()
                .status(report.getStatus())
                .mapping(
                        report.getMappings()
                                .stream()
                                .map(SubmissionMappingDto::from)
                                .collect(Collectors.toList()))
                .log(LogNodeDto.from(report.getLog()))
                .build();
    }
}
