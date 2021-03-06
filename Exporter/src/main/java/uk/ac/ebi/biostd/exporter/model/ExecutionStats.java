package uk.ac.ebi.biostd.exporter.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import uk.ac.ebi.biostd.exporter.utils.DateUtils;

@Getter
@Builder(toBuilder = true)
public class ExecutionStats {

    @JsonProperty("@startTimeTS")
    private long startTimeTS;

    @JsonProperty("@endTimeTS")
    private long endTimeTS;

    @JsonProperty("@submissions")
    private long submissions;

    @JsonProperty("@ioErrors")
    private long errors;

    @JsonProperty("@threads")
    private long threads;

    private Map<String, Object> metrics;

    @JsonAnyGetter
    private Map<String, Object> getMetrics() {
        return metrics;
    }

    @JsonProperty("@startTime")
    public String getStartTime() {
        return DateUtils.getFromEpochMilliseconds(startTimeTS);
    }

    @JsonProperty("@endTime")
    public String getEndTime() {
        return DateUtils.getFromEpochMilliseconds(endTimeTS);
    }

    @JsonProperty("@elapsedTime")
    public String getElapsedTime() {
        return DateUtils.getElapsedTime(endTimeTS - startTimeTS);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("submissions", submissions)
                .append("errors", errors)
                .append("startTime", getStartTime())
                .append("endTime", getEndTime())
                .append("elapsedTime", getElapsedTime())
                .build();
    }
}
