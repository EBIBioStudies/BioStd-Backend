package uk.ac.ebi.biostd.webapp.application.rest.dto;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BatchSubmitReportDto {

    @Getter
    private List<SubmitReportDto> reports;

    public BatchSubmitReportDto(List<SubmitReportDto> reports) {
        this.reports = Collections.unmodifiableList(reports);
    }

    public void setReports(List<SubmitReportDto> reports) {
        this.reports = Collections.unmodifiableList(reports);
    }
}
