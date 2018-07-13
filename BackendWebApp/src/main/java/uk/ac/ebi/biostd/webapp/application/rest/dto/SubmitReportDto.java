package uk.ac.ebi.biostd.webapp.application.rest.dto;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static uk.ac.ebi.biostd.webapp.application.rest.dto.SubmitStatus.submitStatus;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.SubmissionReport;

@Data
@Builder
public class SubmitReportDto {
    private SubmitStatus status;
    private List<SubmissionMappingDto> mapping;
    private LogNodeDto log;

    public static SubmitReportDto fromSubmissionReport(SubmissionReport report) {
        return SubmitReportDto.builder()
                .status(submitStatus(report.getLog().getLevel().getPriority() < LogNode.Level.ERROR.getPriority()))
                .mapping(
                        emptyIfNull(report.getMappings()).stream()
                                .map(SubmissionMappingDto::from)
                                .collect(Collectors.toList()))
                .log(LogNodeDto.from(report.getLog()))
                .build();
    }

    public static SubmitReportDto fromErrorMessage(String message) {
        return SubmitReportDto.builder()
                .status(SubmitStatus.FAIL)
                .log(LogNodeDto.builder()
                        .level(LogNode.Level.ERROR)
                        .message(message)
                        .build())
                .build();
    }

    public static SubmitReportDto fromLogNode(LogNode logNode) {
        return SubmitReportDto.builder()
                .status(submitStatus(logNode.getLevel() == LogNode.Level.ERROR))
                .log(LogNodeDto.from(logNode))
                .build();
    }
}
