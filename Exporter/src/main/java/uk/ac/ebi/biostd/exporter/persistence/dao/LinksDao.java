package uk.ac.ebi.biostd.exporter.persistence.dao;

import static java.util.Collections.singletonMap;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.model.Attribute;
import uk.ac.ebi.biostd.exporter.model.Link;
import uk.ac.ebi.biostd.exporter.persistence.Queries;
import uk.ac.ebi.biostd.exporter.persistence.mappers.AttributeMapper;
import uk.ac.ebi.biostd.exporter.persistence.mappers.LinkMapper;

@Component
@AllArgsConstructor
public class LinksDao {

    private final Queries queries;
    private final LinkMapper linkMapper;
    private final AttributeMapper attributeMapper;
    private final NamedParameterJdbcTemplate template;

    public List<Link> getLinks(long sectionId) {
        return template.query(queries.getLinksBySectionQuery(), singletonMap("section_id", sectionId), linkMapper);
    }

    public List<Attribute> getLinkAttributes(long linkId) {
        return template.query(queries.getLinkAttributesQuery(), singletonMap("link_id", linkId), attributeMapper);
    }

}
