package uk.ac.ebi.biostd.webapp.application.rest.service;

import org.junit.Test;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListFiltersDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListItemDto;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class PendingSubmissionListFilterTest {
    private static List<PendingSubmissionListItemDto> submissions = Arrays.asList(
            PendingSubmissionListItemDto.builder()
                    .accno("A-BCD-123")
                    .title("A B C D")
                    .rtime(LocalDate.of(2018, 5, 17).atStartOfDay(ZoneId.systemDefault()).toEpochSecond())
                    .build(),

            PendingSubmissionListItemDto.builder()
                    .accno("A-BCD-1234")
                    .title("A B C D E")
                    .rtime(LocalDate.of(2018, 5, 18).atStartOfDay(ZoneId.systemDefault()).toEpochSecond())
                    .build(),

            PendingSubmissionListItemDto.builder()
                    .accno("A-BCD-12345")
                    .title("A B C D E")
                    .rtime(LocalDate.of(2018, 5, 19).atStartOfDay(ZoneId.systemDefault()).toEpochSecond())
                    .build()
    );

    @Test
    public void testNoFilters() {
        Predicate<? super PendingSubmissionListItemDto> predicate = PendingSubmissionListFilter.asPredicate(new PendingSubmissionListFiltersDto());
        List<PendingSubmissionListItemDto> result = submissions.stream().filter(predicate).collect(Collectors.toList());
        assertThat(result.size()).isEqualTo(3);
    }
}
