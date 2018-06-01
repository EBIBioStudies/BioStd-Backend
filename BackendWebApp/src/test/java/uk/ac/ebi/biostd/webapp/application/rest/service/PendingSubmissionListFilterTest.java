package uk.ac.ebi.biostd.webapp.application.rest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.biostd.webapp.application.rest.service.PendingSubmissionListFilter.asPredicate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListFiltersDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListItemDto;

public class PendingSubmissionListFilterTest {
    private static List<PendingSubmissionListItemDto> submissions = Arrays.asList(
            PendingSubmissionListItemDto.builder()
                    .accno("A-BCD-123")
                    .title("A B C D")
                    .rtime(seconds(2018, 5, 17))
                    .build(),

            PendingSubmissionListItemDto.builder()
                    .accno("A-BCD-1234")
                    .title("A B C D E")
                    .rtime(seconds(2018, 5, 18))
                    .build(),

            PendingSubmissionListItemDto.builder()
                    .accno("A-BCD-12345")
                    .title("A B C D E F")
                    .rtime(seconds(2018, 5, 19))
                    .build()
    );

    @Test
    public void testNoFilters() {
        List<PendingSubmissionListItemDto> result = search(new PendingSubmissionListFiltersDto());
        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    public void testAccNoFilter() {
        PendingSubmissionListFiltersDto filters = new PendingSubmissionListFiltersDto();

        filters.setAccNo("A-BCD-123");
        List<PendingSubmissionListItemDto> result = search(filters);
        assertThat(result.size()).isEqualTo(1);

        filters.setAccNo("A-BCD-1234*");
        result = search(filters);
        assertThat(result.size()).isEqualTo(2);

        filters.setAccNo("*123*");
        result = search(filters);
        assertThat(result.size()).isEqualTo(3);

        filters.setAccNo("A-BCD");
        result = search(filters);
        assertThat(result.size()).isEqualTo(0);

        filters.setAccNo("");
        result = search(filters);
        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    public void testKeywordsFilter() {
        PendingSubmissionListFiltersDto filters = new PendingSubmissionListFiltersDto();

        filters.setKeywords("A B C D E");
        List<PendingSubmissionListItemDto> result = search(filters);
        assertThat(result.size()).isEqualTo(2);

        filters.setKeywords("F");
        result = search(filters);
        assertThat(result.size()).isEqualTo(1);

        filters.setKeywords("");
        result = search(filters);
        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    public void testRTimeFromFilter() {
        PendingSubmissionListFiltersDto filters = new PendingSubmissionListFiltersDto();

        filters.setRTimeFrom(seconds(2018, 5, 17));
        List<PendingSubmissionListItemDto> result = search(filters);
        assertThat(result.size()).isEqualTo(3);

        filters.setRTimeFrom(seconds(2018, 5, 18));
        result = search(filters);
        assertThat(result.size()).isEqualTo(2);

        filters.setRTimeFrom(seconds(2018, 5, 20));
        result = search(filters);
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    public void testRTimeToFilter() {
        PendingSubmissionListFiltersDto filters = new PendingSubmissionListFiltersDto();

        filters.setRTimeTo(seconds(2018, 5, 20));
        List<PendingSubmissionListItemDto> result = search(filters);
        assertThat(result.size()).isEqualTo(3);

        filters.setRTimeTo(seconds(2018, 5, 19));
        result = search(filters);
        assertThat(result.size()).isEqualTo(2);

        filters.setRTimeTo(seconds(2018, 5, 17));
        result = search(filters);
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    public void testMultipleFilters() {
        PendingSubmissionListFiltersDto filters = new PendingSubmissionListFiltersDto();

        filters.setAccNo("A-BCD-12*");
        filters.setKeywords("A E");
        filters.setRTimeFrom(seconds(2018, 5, 18));
        filters.setRTimeTo(seconds(2018, 5, 19));
        List<PendingSubmissionListItemDto> result = search(filters);
        assertThat(result.size()).isEqualTo(1);
    }

    private static List<PendingSubmissionListItemDto> search(PendingSubmissionListFiltersDto filters) {
        return submissions.stream()
                .filter(asPredicate(filters))
                .collect(Collectors.toList());
    }

    private static long seconds(int year, int month, int day) {
        return LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }
}
