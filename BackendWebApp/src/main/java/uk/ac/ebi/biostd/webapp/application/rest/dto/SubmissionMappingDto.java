package uk.ac.ebi.biostd.webapp.application.rest.dto;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.biostd.in.SubmissionMapping;

@Builder
@Data
public class SubmissionMappingDto {
    private Integer order;

    @Builder.Default
    private String original = "";

    @Builder.Default
    private String assigned = "";

    private List<SectionMappingDto> sections;

    public static SubmissionMappingDto from(SubmissionMapping mapping) {
        return SubmissionMappingDto.builder()
                .order(mapping.getSubmissionMapping().getPosition()[0])
                .original(mapping.getSubmissionMapping().getOrigAcc())
                .assigned(mapping.getSubmissionMapping().getAssignedAcc())
                .sections(mapping.getSectionsMapping().stream()
                        .map(SectionMappingDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}