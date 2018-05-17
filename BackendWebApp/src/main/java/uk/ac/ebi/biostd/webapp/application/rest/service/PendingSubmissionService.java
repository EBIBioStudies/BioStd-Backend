package uk.ac.ebi.biostd.webapp.application.rest.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.domain.services.UserDataService;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserData;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListFiltersDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListItemDto;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class PendingSubmissionService {

    private static final String TOPIC = "submission";

    private static final Comparator<PendingSubmissionListItemDto> SORT_BY_MTIME = (o1, o2) -> {
        Long l1 = o1.getMtime();
        Long l2 = o2.getMtime();
        if (l1 == null && l2 == null) {
            return 0;
        } else if (l1 == null) {
            return 1;
        } else if (l2 == null) {
            return -1;
        }
        return l2.compareTo(l1);
    };

    private final UserDataService userDataService;
    private final PendingSubmissionUtil pendingSubmissionUtil;

    public PendingSubmissionListDto getSubmissions(PendingSubmissionListFiltersDto filters, User user) {
        Predicate<? super PendingSubmissionListItemDto> predicate = new PendingSubmissionListFilter(filters).asPredicate();

        List<PendingSubmissionListItemDto> submissions = userDataService.findAllByUserAndTopic(user.getId(), TOPIC)
                .stream()
                .map(UserData::getData)
                .map(pendingSubmissionUtil::parse)
                .map(o -> o.flatMap(pendingSubmissionUtil::convert))
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .sorted(SORT_BY_MTIME)
                .filter(predicate::test)
                .skip(filters.getOffset())
                .limit(filters.getLimit())
                .collect(toList());

        return new PendingSubmissionListDto(submissions);
    }

}
