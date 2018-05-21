package uk.ac.ebi.biostd.webapp.application.rest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionDto;
import uk.ac.ebi.biostd.webapp.application.rest.dto.PendingSubmissionListItemDto;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Component
@Slf4j
@AllArgsConstructor
public class PendingSubmissionUtil {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final String ACCNO_PREFIX = "TMP_";

    private final ObjectMapper objectMapper;

    public String asString(PendingSubmissionDto dto) {
        return objectMapper.valueToTree(dto).toString();
    }

    public Optional<PendingSubmissionDto> parse(String data) {
        try {
            return Optional.of(objectMapper.readValue(data, PendingSubmissionDto.class));
        } catch (IOException e) {
            log.error("error while parsing pending submission", e);
        }
        return Optional.empty();
    }

    public PendingSubmissionListItemDto convert(PendingSubmissionDto pendingSubmission) {
        JsonNode node = pendingSubmission.getData();
        return PendingSubmissionListItemDto.builder()
                .accno(pendingSubmission.getAccno())
                .mtime(pendingSubmission.getModificationTimeInSeconds())
                .rtime(getReleaseTimeInSeconds(node).orElse(null))
                .title(getTitle(node).orElse(""))
                .build();
    }

    public Optional<PendingSubmissionDto> create(String data) {
        try {
            JsonNode node = objectMapper.readTree(data);
            PendingSubmissionDto submission = new PendingSubmissionDto();
            submission.setAccno(getAccno(node).orElse(newAccno()));
            submission.setData(node);
            return Optional.of(submission);

        } catch (IOException e) {
            log.error("error while creating pending submission", e);
        }
        return Optional.empty();
    }

    static private String newAccno() {
        return ACCNO_PREFIX + System.currentTimeMillis();
    }

    static private Optional<String> getAccno(JsonNode jsonNode) {
        return Optional.ofNullable(jsonNode.get("accno")).map(JsonNode::asText).map(String::trim).filter(s -> !s.isEmpty());
    }

    static private Optional<String> getTitle(JsonNode jsonNode) {
        Optional<JsonNode> attrValueNode = attributes(jsonNode)
                .stream()
                .filter(attrNameFilter("title"))
                .findFirst();
        return attrValueNode.map(v -> attrValue(v, ""));
    }

    static private Optional<Long> getReleaseTimeInSeconds(JsonNode node) {
        Optional<JsonNode> attrValueNode = attributes(node)
                .stream()
                .filter(attrNameFilter("releaseDate"))
                .findFirst();

        return attrValueNode.map(v -> numberOfSeconds(attrValue(v, "")));
    }

    static private List<JsonNode> attributes(JsonNode node) {
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

    static private Predicate<JsonNode> attrNameFilter(String attrName) {
        return jsonNode -> fieldValue(jsonNode, "name", "").equalsIgnoreCase(attrName);
    }

    static private String attrValue(JsonNode jsonNode, String deflt) {
        return fieldValue(jsonNode, "value", deflt);
    }

    static private String fieldValue(JsonNode jsonNode, String fieldName, String deflt) {
        JsonNode valueNode = jsonNode.get(fieldName);
        return Optional.ofNullable(valueNode).map(JsonNode::asText).orElse(deflt);
    }

    static private Long numberOfSeconds(String date) {
        try {
            return DATE_FORMAT.parse(date).getTime() / 1000;
        } catch (ParseException e) {
            log.error("error while parsing date", date, e);
        }
        return null;
    }
}
