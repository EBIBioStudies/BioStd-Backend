package uk.ac.ebi.biostd.webapp.application.rest.dto;

import com.pri.util.collection.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.biostd.treelog.LogNode;

@Builder
@Data
public class LogNodeDto {

    private String level;

    @Builder.Default
    private String message = "";

    private List<LogNodeDto> subnodes;

    public static LogNodeDto from(LogNode log) {
        return LogNodeDto.builder()
                .level(log.getLevel().name())
                .message(log.getMessage())
                .subnodes(Optional.ofNullable(log.getSubNodes()).orElse(Collections.emptyList()).stream()
                        .map(LogNodeDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
