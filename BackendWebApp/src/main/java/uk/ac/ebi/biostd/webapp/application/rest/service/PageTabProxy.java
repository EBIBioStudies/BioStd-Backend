package uk.ac.ebi.biostd.webapp.application.rest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PageTabProxy {
    private static String ACCNO_FIELD = "accno";
    private static String NAME_FIELD = "name";
    private static String VALUE_FIELD = "value";
    private static String ATTRIBUTES_FIELD = "attributes";
    private static String SECTION_FIELD = "section";
    private static String TITLE_ATTRIBUTE = "title";
    private static String RELEASE_DATE_ATTRIBUTE = "releaseDate";
    private static String ATTACH_TO_ATTRIBUTE = "attachTo";

    private JsonNode pageTab;
    private JsonNode root;

    public PageTabProxy(JsonNode root) {
        this.root = root;

        Iterable<Map.Entry<String, JsonNode>> iterable = () -> root.fields();
        this.pageTab = StreamSupport.stream(iterable.spliterator(), false)
                .filter(field -> field.getKey().equalsIgnoreCase("submissions"))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(root);
    }

    public Optional<String> getAccno() {
        return getTextField(pageTab, ACCNO_FIELD);
    }

    public PageTabProxy setAccno(String value) {
        setTextField(pageTab, ACCNO_FIELD, value);
        return this;
    }

    public Optional<String> getTitle() {
        return getAttribute(pageTab, TITLE_ATTRIBUTE).stream().findFirst();
    }

    public Optional<String> getReleaseDate() {
        return getAttribute(pageTab, RELEASE_DATE_ATTRIBUTE).stream().findFirst();
    }

    public Set<String> getAttachToAttr() {
        return getAttribute(pageTab, ATTACH_TO_ATTRIBUTE);
    }

    public PageTabProxy setAttachToAttr(Set<String> values, ObjectMapper objectMapper) {
        setAttribute(pageTab, ATTACH_TO_ATTRIBUTE, values, objectMapper);
        return this;
    }

    public JsonNode json() {
        return root;
    }

    public JsonNode wrappedJson(ObjectMapper objectMapper) {
        if (root != pageTab) {
            return root;
        }

        return objectMapper.createObjectNode()
                .set("submissions", objectMapper.createArrayNode().add(pageTab));

    }

    private Optional<String> getTextField(JsonNode node, String propertyName) {
        return Optional.ofNullable(node.get(propertyName)).map(JsonNode::asText).map(String::trim);
    }

    private void setTextField(JsonNode node, String propertyName, String propertyValue) {
        ((ObjectNode) node).put(propertyName, propertyValue);
    }

    private Set<String> getAttribute(JsonNode node, String attrName) {
        return getAttributes(node)
                .stream()
                .filter(attrNameFilter(attrName))
                .map(this::getAttrValue)
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toSet());
    }

    private void setAttribute(JsonNode node, String attrName, Set<String> values, ObjectMapper objectMapper) {
        List<JsonNode> attrNodes = Stream.concat(
                getAttributes(node)
                        .stream()
                        .filter(attrNameFilter(attrName).negate()),
                values.stream().map(value ->
                        objectMapper.createObjectNode().put(NAME_FIELD, attrName).put(VALUE_FIELD, value)
                )).collect(Collectors.toList());

        ((ObjectNode) node).set(ATTRIBUTES_FIELD, objectMapper.createArrayNode().addAll(attrNodes));
    }

    private List<JsonNode> getAttributes(JsonNode node) {
        List<JsonNode> list = new ArrayList<>();
        final String attributesProp = ATTRIBUTES_FIELD;
        if (node.has(attributesProp)) {
            JsonNode attributesNode = node.get(attributesProp);
            if (attributesNode.isArray()) {
                attributesNode.iterator().forEachRemaining(list::add);
            }
        }
        // NB: only submission node has 'section' property
        Optional.ofNullable(node.get(SECTION_FIELD)).ifPresent(sectionNode -> {
            list.addAll(getAttributes(sectionNode));
        });
        return list;
    }

    private Predicate<JsonNode> attrNameFilter(String attrName) {
        return jsonNode -> getTextField(jsonNode, NAME_FIELD).orElse("").equalsIgnoreCase(attrName);
    }

    private Optional<String> getAttrValue(JsonNode jsonNode) {
        return getTextField(jsonNode, VALUE_FIELD);
    }
}
