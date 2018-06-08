package uk.ac.ebi.biostd.webapp.application.rest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PageTabProxy {

    private final JsonNode pageTab;

    public PageTabProxy(JsonNode pageTab) {
        this.pageTab = pageTab;
    }

    public Optional<String> getAccno() {
        return Optional.ofNullable(pageTab.get("accno")).map(JsonNode::asText).map(String::trim).filter(s -> !s.isEmpty());
    }

    public Optional<String> getTitle() {
        return attributes(pageTab)
                .stream()
                .filter(attrNameFilter("title"))
                .findFirst()
                .map(attrNode -> attrValue(attrNode, ""));
    }

    public Optional<String> getReleaseDate() {
        return attributes(pageTab)
                .stream()
                .filter(attrNameFilter("releaseDate"))
                .findFirst()
                .map(attrNode -> attrValue(attrNode, ""));
    }

    public List<String> attachToAttr() {
        return attributes(pageTab).stream()
                .filter(attrNameFilter("attachto"))
                .map(attrNode -> attrValue(attrNode, ""))
                .filter(String::isEmpty)
                .collect(Collectors.toList());
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

    public JsonNode amendAccno(String accnoTemplate) {
        return ((ObjectNode)pageTab).put("accno", accnoTemplate);
    }
}
