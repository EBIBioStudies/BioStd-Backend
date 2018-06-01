package uk.ac.ebi.biostd.webapp.application.rest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListItemDto;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Component
@Slf4j
@AllArgsConstructor
public class PendingSubmissionUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String ACCNO_PREFIX = "TMP_";

    private final ObjectMapper objectMapper;

    public String asString(PendingSubmissionDto dto) {
        return objectMapper.valueToTree(dto).toString();
    }

    public Optional<PendingSubmissionDto> asPendingSubmission(String data) {
        try {
            return Optional.of(objectMapper.readValue(data, PendingSubmissionDto.class));
        } catch (IOException e) {
            log.error("error while parsing pending submission", e);
        }
        return Optional.empty();
    }

    public PendingSubmissionListItemDto convertToPendingSubmissionListItem(PendingSubmissionDto pendingSubmission) {
        JsonNode node = pendingSubmission.getData();
        return PendingSubmissionListItemDto.builder()
                .accno(pendingSubmission.getAccno())
                .mtime(pendingSubmission.getModificationTimeInSeconds())
                .rtime(getReleaseTimeInSeconds(node).orElse(null))
                .title(getTitle(node).orElse(""))
                .build();
    }

    public Optional<PendingSubmissionDto> createPendingSubmission(String pageTab) {
        try {
            JsonNode node = objectMapper.readTree(pageTab);
            PendingSubmissionDto submission = new PendingSubmissionDto();
            submission.setAccno(getAccno(node).orElse(newAccno()));
            submission.setData(node);
            submission.setChanged(System.currentTimeMillis());
            return Optional.of(submission);

        } catch (IOException e) {
            log.error("error while creating pending submission", e);
        }
        return Optional.empty();
    }

    private String newAccno() {
        return ACCNO_PREFIX + System.currentTimeMillis();
    }

    private Optional<String> getAccno(JsonNode jsonNode) {
        return Optional.ofNullable(jsonNode.get("accno")).map(JsonNode::asText).map(String::trim).filter(s -> !s.isEmpty());
    }

    private Optional<String> getTitle(JsonNode jsonNode) {
        Optional<JsonNode> attrValueNode = attributes(jsonNode)
                .stream()
                .filter(attrNameFilter("title"))
                .findFirst();
        return attrValueNode.map(v -> attrValue(v, ""));
    }

    private Optional<Long> getReleaseTimeInSeconds(JsonNode node) {
        Optional<JsonNode> attrValueNode = attributes(node)
                .stream()
                .filter(attrNameFilter("releaseDate"))
                .findFirst();

        return attrValueNode.map(v -> numberOfSeconds(attrValue(v, "")));
    }

    private List<JsonNode> attributes(JsonNode node) {
        List<JsonNode> list = new ArrayList<>();
        final String attributesProp = "attributes";
        if (node.has(attributesProp)) {
            JsonNode attributesNode = node.get(attributesProp);
            if (attributesNode.isArray()) {
                attributesNode.iterator().forEachRemaining(list::add);
            }
        }
        // NB: only submission node has 'section' property
        final String sectionProp = "section";
        if (node.has(sectionProp)) {
            list.addAll(attributes(node.get(sectionProp)));
        }
        return list;
    }

    private Predicate<JsonNode> attrNameFilter(String attrName) {
        return jsonNode -> fieldValue(jsonNode, "name", "").equalsIgnoreCase(attrName);
    }

    private String attrValue(JsonNode jsonNode, String deflt) {
        return fieldValue(jsonNode, "value", deflt);
    }

    private String fieldValue(JsonNode jsonNode, String fieldName, String deflt) {
        JsonNode valueNode = jsonNode.get(fieldName);
        return Optional.ofNullable(valueNode).map(JsonNode::asText).orElse(deflt);
    }

    private Long numberOfSeconds(String date) {
        return LocalDate.parse(date, DATE_FORMAT).atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }
}
