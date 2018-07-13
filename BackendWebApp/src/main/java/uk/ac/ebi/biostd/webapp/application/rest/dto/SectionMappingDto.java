package uk.ac.ebi.biostd.webapp.application.rest.dto;

import com.google.common.primitives.Ints;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.biostd.in.AccessionMapping;

@Builder
@Data
public class SectionMappingDto {

    private List<Integer> order;

    @Builder.Default
    private String original = "";

    @Builder.Default
    private String assigned = "";

    public static SectionMappingDto from(AccessionMapping mapping) {
        return SectionMappingDto.builder()
                .order(Ints.asList(mapping.getPosition()))
                .original(mapping.getOrigAcc())
                .assigned(mapping.getAssignedAcc())
                .build();
    }
}
