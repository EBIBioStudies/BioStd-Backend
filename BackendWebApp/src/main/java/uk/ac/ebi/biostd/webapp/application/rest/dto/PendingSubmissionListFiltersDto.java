package uk.ac.ebi.biostd.webapp.application.rest.dto;

import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
public class PendingSubmissionListFiltersDto {
    @Getter
    private Integer offset = 0;

    @Getter
    private Integer limit = 50;

    private String accNo;
    private Long rTimeFrom;
    private Long rTimeTo;
    private String keywords;

    public Optional<String> getAccNo() {
        return Optional.ofNullable(accNo);
    }

    public Optional<Long> getRTimeFrom() {
        return Optional.ofNullable(rTimeFrom);
    }

    public Optional<Long> getRTimeTo() {
        return Optional.ofNullable(rTimeTo);
    }

    public Optional<String> getKeywords() {
        return Optional.ofNullable(keywords);
    }
}
