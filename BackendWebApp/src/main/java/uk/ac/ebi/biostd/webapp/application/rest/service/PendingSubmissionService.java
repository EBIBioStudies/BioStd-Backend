package uk.ac.ebi.biostd.webapp.application.rest.service;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.webapp.application.domain.services.UserDataService;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserData;
import uk.ac.ebi.biostd.webapp.application.rest.dto.*;

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
    private final SubmitService submitService;

    public PendingSubmissionListDto getSubmissionList(PendingSubmissionListFiltersDto filters, User user) {
        Predicate<? super PendingSubmissionListItemDto> predicate = PendingSubmissionListFilter.asPredicate(filters);

        List<PendingSubmissionListItemDto> submissions = userDataService.findAllByUserAndTopic(user.getId(), TOPIC)
                .stream()
                .map(UserData::getData)
                .map(pendingSubmissionUtil::pendingSubmissionFromString)
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .map(pendingSubmissionUtil::pendingSubmissionToListItem)
                .sorted(SORT_BY_MTIME)
                .filter(predicate::test)
                .skip(filters.getOffset())
                .limit(filters.getLimit())
                .collect(toList());

        return new PendingSubmissionListDto(submissions);
    }

    public Optional<PendingSubmissionDto> getSubmissionByAccNo(String accno, User user) {
        return findByAccNoAndUser(accno, user);
    }

    public void deleteSubmissionByAccNo(String accno, User user) {
        userDataService.deleteModifiedSubmission(user.getId(), accno);
    }

    public Optional<PendingSubmissionDto> updateSubmission(String accno, JsonNode pageTab, User user) {
        return findByAccNoAndUser(accno, user)
                .flatMap(subm -> this.update(subm, pageTab, user));
    }

    public Optional<PendingSubmissionDto> createSubmission(JsonNode pageTab, User user) {
        PendingSubmissionDto subm = pendingSubmissionUtil.createPendingSubmission(pageTab);
        return this.update(subm, pageTab, user);
    }

    public Optional<SubmitReportDto> submitSubmission(String accno, User user) {
        return findByAccNoAndUser(accno, user)
                .map(subm -> this.submit(subm, user));
    }

    private SubmitReportDto submit(PendingSubmissionDto dto, User user) {
        boolean isNew = pendingSubmissionUtil.isTemporaryAccno(dto.getAccno());
        SubmitOperation operation = isNew ? SubmitOperation.CREATE : SubmitOperation.CREATE_OR_UPDATE;

        SubmitReportDto report = submitService.submitJson(dto.getData(), operation, user);
        if (report.getStatus() == SubmitStatus.OK) {
            deleteSubmissionByAccNo(dto.getAccno(), user);
        }
        return report;
    }

    private Optional<PendingSubmissionDto> update(PendingSubmissionDto original, JsonNode data, User user) {
        PendingSubmissionDto updated = new PendingSubmissionDto();
        updated.setAccno(original.getAccno());
        updated.setChanged(System.currentTimeMillis());
        updated.setData(data);

        UserData userData =
                userDataService.update(user.getId(), updated.getAccno(), pendingSubmissionUtil.pendingSubmissionToString(updated), TOPIC);
        return pendingSubmissionUtil.pendingSubmissionFromString(userData.getData());
    }

    private Optional<PendingSubmissionDto> findByAccNoAndUser(String accno, User user) {
        return userDataService.findByUserAndKey(user.getId(), accno)
                .map(UserData::getData)
                .flatMap(pendingSubmissionUtil::pendingSubmissionFromString);
    }

}
