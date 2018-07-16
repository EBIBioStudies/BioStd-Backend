package uk.ac.ebi.biostd.webapp.application.rest.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.biostd.webapp.application.configuration.WebConfiguration;
import uk.ac.ebi.biostd.webapp.application.rest.dto.SubmissionMappingDto;

@RunWith(SpringRunner.class)
@Import(WebConfiguration.class)
public class PageTabProxyTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testEmptyProxy() throws IOException {
        Stream.of(
                new PageTabProxy(null),
                new PageTabProxy(objectMapper.readTree("{ submissions:[] }")))
                .forEach(proxy -> {
                    assertThat(proxy.getAccno().isPresent()).isFalse();
                    assertThat(proxy.getTitle().isPresent()).isFalse();
                    assertThat(proxy.getReleaseDate().isPresent()).isFalse();
                    assertThat(proxy.getAttachToAttr().isEmpty()).isTrue();

                    proxy.setAccno("1234");
                    assertThat(proxy.getAccno().isPresent()).isFalse();

                    proxy.setAttachToAttr(ImmutableSet.of("1", "2"), objectMapper);
                    assertThat(proxy.getAttachToAttr().isEmpty()).isTrue();
                });
    }

    @Test
    public void testAccno() throws IOException {
        Stream.of(
                new PageTabProxy(objectMapper.readTree("{}")),
                new PageTabProxy(objectMapper.readTree("{ submissions:[ {} ] }")))
                .forEach(proxy -> {
                    assertThat(proxy.getAccno().isPresent()).isFalse();

                    proxy.setAccnoIfEmpty("123");
                    assertThat(proxy.getAccno().orElse(null)).isEqualTo("123");

                    proxy.setAccnoIfEmpty("1234");
                    assertThat(proxy.getAccno().orElse(null)).isEqualTo("123");

                    proxy.setAccno("1234");
                    assertThat(proxy.getAccno().orElse(null)).isEqualTo("1234");
                });
    }

    @Test
    public void testAttachToAttributes() throws IOException {
        Stream.of(
                new PageTabProxy(objectMapper.readTree("{}")),
                new PageTabProxy(objectMapper.readTree("{ submissions:[ {} ] }")))
                .forEach(proxy -> {
                    assertThat(proxy.getAttachToAttr().isEmpty()).isTrue();

                    Set<String> testSet = ImmutableSet.of("1", "2");
                    proxy.setAttachToAttr(testSet, objectMapper);
                    assertThat(proxy.getAttachToAttr().size()).isEqualTo(testSet.size());
                    assertThat(proxy.getAttachToAttr().containsAll(testSet)).isTrue();
                });
    }

    @Test
    public void testSubmissionsWrap() throws IOException {
        JsonNode origNode = objectMapper.readTree("{ submissions:[ {} ] }");

        PageTabProxy proxy = new PageTabProxy(origNode);
        JsonNode wrapNode = proxy.wrappedJson(objectMapper);
        assertThat(wrapNode).isEqualTo(origNode);


        origNode = objectMapper.readTree("{}");
        proxy = new PageTabProxy(origNode);
        wrapNode = proxy.wrappedJson(objectMapper);
        assertThat(wrapNode).isNotEqualTo(origNode);

        assertThat(wrapNode.has("submissions")).isTrue();
        assertThat(wrapNode.get("submissions").get(0)).isEqualTo(origNode);
    }

    @Test
    public void testBuilderDefaults() {
        SubmissionMappingDto dto1 = SubmissionMappingDto.builder().build();
        SubmissionMappingDto dto2 = SubmissionMappingDto.builder().original(null).build();
        assertThat(dto2).isNotNull();

    }

}
