package uk.ac.ebi.biostd.webapp.application.rest.service;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.model.AbstractAttribute;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.SimpleLogNode;
import uk.ac.ebi.biostd.treelog.SubmissionReport;
import uk.ac.ebi.biostd.util.DataFormat;
import uk.ac.ebi.biostd.webapp.application.domain.services.UserDataService;
import uk.ac.ebi.biostd.webapp.application.persitence.entities.UserData;
import uk.ac.ebi.biostd.webapp.application.rest.dto.*;
import uk.ac.ebi.biostd.webapp.server.mng.SubmissionManager;
import uk.ac.ebi.biostd.webapp.server.mng.impl.JPASubmissionManager;

@Service
@AllArgsConstructor
public class PendingSubmissionService {

    private static final String DEFAULT_ACCNO_TEMPLATE = "!{S-BSST}";
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
    private final PendingSubmissionUtil util;
    private final JPASubmissionManager submissionManager;
    private final ObjectMapper objectMapper;

    public PendingSubmissionListDto getSubmissionList(PendingSubmissionListFiltersDto filters, User user) {
        Predicate<? super PendingSubmissionListItemDto> predicate = PendingSubmissionListFilter.asPredicate(filters);

        List<PendingSubmissionListItemDto> submissions = userDataService.findAllByUserAndTopic(user.getId(), TOPIC)
                .stream()
                .map(UserData::getData)
                .map(util::pendingSubmissionFromString)
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .map(util::pendingSubmissionToListItem)
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
                .map(subm -> this.update(subm, pageTab, user));
    }

    public PendingSubmissionDto createSubmission(JsonNode pageTab, User user) {
        PendingSubmissionDto subm = util.createPendingSubmission(pageTab);
        return this.update(subm, pageTab, user);
    }

    public Optional<SubmissionReportDto> submitSubmission(String accno, User user) {
        return findByAccNoAndUser(accno, user)
                .map(subm -> this.submit(subm, user));
    }

    private SubmissionReportDto submit(PendingSubmissionDto dto, User user) {
        boolean isNew = util.isTemporaryAccno(dto.getAccno());

        JsonNode data = multiSubmissionsWrap(isNew ? amendAccno(dto.getData()) : dto.getData());

        SubmissionManager.Operation operation = isNew ? SubmissionManager.Operation.CREATE : SubmissionManager.Operation.UPDATE;

        //-- TODO: calling legacy code here; taken from SubmitServlet unchanged
        SubmissionReport report = submissionManager.createSubmission(
                data.toString().getBytes(), DataFormat.json, Charset.defaultCharset().toString(), operation, user,
                false, false, null);
        SimpleLogNode.setLevels(report.getLog());

        if (report.getLog().getLevel() != LogNode.Level.ERROR) {
            deleteSubmissionByAccNo(dto.getAccno(), user);
        }
        return SubmissionReportDto.from(report);
    }

    private JsonNode multiSubmissionsWrap(JsonNode pageTab) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        arrayNode.add(pageTab);
        return objectMapper.createObjectNode().set("submissions", arrayNode);
    }

    private JsonNode amendAccno(JsonNode pageTab) {
        PageTabProxy pageTabProxy = new PageTabProxy(pageTab);
        List<String> attachToAccessions = pageTabProxy.attachToAttr();

        String accnoTemplate = attachToAccessions.size() == 1 ?
                getAccnoTemplate(attachToAccessions.get(0)) : DEFAULT_ACCNO_TEMPLATE;

        return pageTabProxy.amendAccno(accnoTemplate);
    }

    private String getAccnoTemplate(String parentAccno) {
        Submission subm = submissionManager.getSubmissionsByAccession(parentAccno);
        return subm.getAttributes().stream()
                .filter(attr -> attr.getName().equalsIgnoreCase("accnotemplate"))
                .map(AbstractAttribute::getValue)
                .findFirst()
                .orElse(DEFAULT_ACCNO_TEMPLATE);
    }

    private PendingSubmissionDto update(PendingSubmissionDto original, JsonNode data, User user) {
        PendingSubmissionDto updated = new PendingSubmissionDto();
        updated.setAccno(original.getAccno());
        updated.setChanged(System.currentTimeMillis());
        updated.setData(data);

        userDataService.update(user.getId(), updated.getAccno(), util.pendingSubmissionToString(updated), TOPIC);
        return updated;
    }

    private Optional<PendingSubmissionDto> findByAccNoAndUser(String accno, User user) {
        return userDataService.findByUserAndKey(user.getId(), accno)
                .map(UserData::getData)
                .flatMap(util::pendingSubmissionFromString);
    }

}
