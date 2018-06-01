package uk.ac.ebi.biostd.webapp.application.rest.service;

import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListFiltersDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListItemDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PendingSubmissionListFilter {

    public static Predicate<? super PendingSubmissionListItemDto> asPredicate(PendingSubmissionListFiltersDto filters) {
        List<Predicate<? super PendingSubmissionListItemDto>> predicates = new ArrayList<>();

        Optional.ofNullable(filters.getAccNo())
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .ifPresent(v -> predicates.add(accNoFilter(v)));

        Optional.ofNullable(filters.getKeywords())
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .ifPresent(v -> predicates.add(keywordsFilter(v)));

        Optional.ofNullable(filters.getRTimeFrom()).ifPresent(v -> predicates.add(rTimeFromFilter(v)));
        Optional.ofNullable(filters.getRTimeTo()).ifPresent(v -> predicates.add(rTimeToFilter(v)));

        return item -> predicates.stream().allMatch(p -> p.test(item));
    }

    private static Predicate<? super PendingSubmissionListItemDto> rTimeFromFilter(final Long dateInSeconds) {
        return (Predicate<PendingSubmissionListItemDto>) dto -> {
            Long rtime = dto.getRtime();
            return rtime != null && rtime.compareTo(dateInSeconds) >= 0;
        };
    }

    private static Predicate<? super PendingSubmissionListItemDto> rTimeToFilter(final Long dateInSeconds) {
        return (Predicate<PendingSubmissionListItemDto>) dto -> {
            Long rtime = dto.getRtime();
            return rtime != null && rtime.compareTo(dateInSeconds) < 0;
        };
    }

    private static Predicate<? super PendingSubmissionListItemDto> keywordsFilter(String keywordsAsString) {
        final List<Predicate<String>> keywords = Arrays.stream(keywordsAsString.split(" "))
                .filter(k -> !k.isEmpty())
                .map(k -> (Predicate<String>) str -> str != null && str.contains(k))
                .collect(Collectors.toList());

        return (Predicate<PendingSubmissionListItemDto>) dto -> {
            String title = dto.getTitle();
            return title != null && keywords.stream().allMatch(p -> p.test(title));
        };
    }

    private static Predicate<? super PendingSubmissionListItemDto> accNoFilter(String accNoPattern) {
        final Pattern pattern = Pattern.compile(fromWildcard(accNoPattern));
        return (Predicate<PendingSubmissionListItemDto>) dto -> {
            String accno = dto.getAccno();
            return accno != null && pattern.matcher(accno).matches();
        };
    }

    private static String fromWildcard(String wildcard) {
        Pattern regex = Pattern.compile("[^*]+|(\\*)|(\\?)");
        Matcher m = regex.matcher(wildcard);
        StringBuffer b = new StringBuffer();
        while (m.find()) {
            if (m.group(1) != null) m.appendReplacement(b, ".*");
            else if (m.group(2) != null) m.appendReplacement(b, ".");
            else m.appendReplacement(b, "\\\\Q" + m.group(0) + "\\\\E");
        }
        m.appendTail(b);
        return b.toString();
    }
}