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

    private interface InnerProxy {
        Optional<String> getTextField(String name);

        void setTextField(String name, String value);

        Set<String> getAttribute(String name);

        void setAttribute(String name, Set<String> values, ObjectMapper objectMapper);

        JsonNode pageTab();
    }

    private static class InnerProxyImpl implements InnerProxy {

        private final JsonNode pageTab;

        private InnerProxyImpl(JsonNode pageTab) {
            this.pageTab = pageTab;
        }

        @Override
        public Optional<String> getTextField(String propertyName) {
            return getNodeTextField(pageTab, propertyName);
        }

        @Override
        public void setTextField(String propertyName, String propertyValue) {
            ((ObjectNode) pageTab).put(propertyName, propertyValue);
        }

        @Override
        public Set<String> getAttribute(String attrName) {
            return getAttributes(pageTab)
                    .stream()
                    .filter(attrNameFilter(attrName))
                    .map(this::getAttrValue)
                    .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                    .collect(Collectors.toSet());
        }

        @Override
        public void setAttribute(String attrName, Set<String> values, ObjectMapper objectMapper) {
            List<JsonNode> attrNodes = Stream.concat(
                    getAttributes(pageTab)
                            .stream()
                            .filter(attrNameFilter(attrName).negate()),
                    values.stream().map(value ->
                            objectMapper.createObjectNode().put(NAME_FIELD, attrName).put(VALUE_FIELD, value)
                    )).collect(Collectors.toList());

            ((ObjectNode) pageTab).set(ATTRIBUTES_FIELD, objectMapper.createArrayNode().addAll(attrNodes));
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
            return jsonNode -> getNodeTextField(jsonNode, NAME_FIELD).orElse("").equalsIgnoreCase(attrName);
        }

        private Optional<String> getAttrValue(JsonNode jsonNode) {
            return getNodeTextField(jsonNode, VALUE_FIELD);
        }

        private Optional<String> getNodeTextField(JsonNode node, String fieldName) {
            return Optional.ofNullable(node.get(fieldName)).map(JsonNode::asText).map(String::trim);
        }

        @Override
        public JsonNode pageTab() {
            return pageTab;
        }
    }

    private static final InnerProxy EMPTY_INNER_PROXY = new InnerProxy() {
        @Override
        public Optional<String> getTextField(String name) {
            return Optional.empty();
        }

        @Override
        public void setTextField(String name, String value) {
        }

        @Override
        public Set<String> getAttribute(String name) {
            return Collections.emptySet();
        }

        @Override
        public void setAttribute(String name, Set<String> values, ObjectMapper objectMapper) {
        }

        @Override
        public JsonNode pageTab() {
            return null;
        }
    };

    private static final String ACCNO_FIELD = "accno";
    private static final String NAME_FIELD = "name";
    private static final String VALUE_FIELD = "value";
    private static final String ATTRIBUTES_FIELD = "attributes";
    private static final String SECTION_FIELD = "section";
    private static final String TITLE_ATTRIBUTE = "Title";
    private static final String RELEASE_DATE_ATTRIBUTE = "ReleaseDate";
    private static final String ATTACH_TO_ATTRIBUTE = "AttachTo";

    private final JsonNode root;
    private final InnerProxy innerProxy;

    public PageTabProxy(JsonNode node) {
        this.root = node;

        Optional<JsonNode> opNode = Optional.ofNullable(node);
        Iterable<Map.Entry<String, JsonNode>> iterable = () -> opNode.map(JsonNode::fields).orElse(Collections.emptyIterator());

        Optional<JsonNode> submissions = StreamSupport.stream(iterable.spliterator(), false)
                .filter(field -> field.getKey().equalsIgnoreCase("submissions"))
                .findFirst()
                .map(Map.Entry::getValue);

        JsonNode ptNode = submissions.isPresent() ?
                submissions
                        .filter(arrayNode -> arrayNode.has(0))
                        .map(arrayNode -> arrayNode.get(0))
                        .orElse(null) : node;

        innerProxy = createInnerProxy(ptNode);
    }

    public Optional<String> getAccno() {
        return innerProxy.getTextField(ACCNO_FIELD)
                .filter(v -> !v.isEmpty());
    }

    public PageTabProxy setAccno(String value) {
        innerProxy.setTextField(ACCNO_FIELD, value);
        return this;
    }

    public PageTabProxy setAccnoIfEmpty(String value) {
        return getAccno().map(v -> this).orElseGet(() -> setAccno(value));
    }

    public Optional<String> getTitle() {
        return innerProxy.getAttribute(TITLE_ATTRIBUTE).stream().findFirst();
    }

    public Optional<String> getReleaseDate() {
        return innerProxy.getAttribute(RELEASE_DATE_ATTRIBUTE).stream().filter(v -> !v.trim().isEmpty()).findFirst();
    }

    public Set<String> getAttachToAttr() {
        return innerProxy.getAttribute(ATTACH_TO_ATTRIBUTE);
    }

    public PageTabProxy setAttachToAttr(Set<String> values, ObjectMapper objectMapper) {
        innerProxy.setAttribute(ATTACH_TO_ATTRIBUTE, values, objectMapper);
        return this;
    }

    public PageTabProxy addAttachToAttr(Collection<String> values, ObjectMapper objectMapper) {
        return setAttachToAttr(
                Stream.concat(getAttachToAttr().stream(), values.stream())
                        .collect(Collectors.toSet()), objectMapper);
    }

    public JsonNode json() {
        return root;
    }

    public JsonNode wrappedJson(ObjectMapper objectMapper) {
        if (root != innerProxy.pageTab()) {
            return root;
        }

        return objectMapper.createObjectNode()
                .set("submissions", objectMapper.createArrayNode().add(innerProxy.pageTab()));
    }

    private static InnerProxy createInnerProxy(JsonNode node) {
        return Optional.ofNullable(node)
                .map(n -> (InnerProxy) new InnerProxyImpl(n))
                .orElse(EMPTY_INNER_PROXY);
    }
}
