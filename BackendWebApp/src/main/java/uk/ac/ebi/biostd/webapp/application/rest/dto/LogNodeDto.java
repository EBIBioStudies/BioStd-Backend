package uk.ac.ebi.biostd.webapp.application.rest.dto;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.biostd.treelog.LogNode;

@Builder
@Data
public class LogNodeDto {

    private LogNode.Level level;

    @Builder.Default
    private String message = "";

    private List<LogNodeDto> subnodes;

    public static LogNodeDto from(LogNode log) {
        return LogNodeDto.builder()
                .level(log.getLevel())
                .message(log.getMessage())
                .subnodes(emptyIfNull(log.getSubNodes()).stream()
                        .map(LogNodeDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
