package uk.ac.ebi.biostd.exporter.persistence.dao;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.model.Attribute;
import uk.ac.ebi.biostd.exporter.model.File;
import uk.ac.ebi.biostd.exporter.model.Section;
import uk.ac.ebi.biostd.exporter.persistence.Queries;
import uk.ac.ebi.biostd.exporter.persistence.mappers.AttributeMapper;
import uk.ac.ebi.biostd.exporter.persistence.mappers.FileMapper;
import uk.ac.ebi.biostd.exporter.persistence.mappers.SectionMapper;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;

@Component
@AllArgsConstructor
public class SectionDao {
    private static final String SEC_ID_PARAM = "section_id";

    private final Queries queries;
    private final NamedParameterJdbcTemplate template;
    private final AttributeMapper attributeMapper;
    private final SectionMapper sectionMapper;
    private final FileMapper fileMapper;

    public List<Attribute> getSectionAttributes(long sectionId) {
        return template.query(queries.getSectionAttributesQuery(), sectionId(sectionId), attributeMapper);
    }

    public List<File> getSectionFiles(long sectionId) {
        return template.query(queries.getSectionFilesQuery(), sectionId(sectionId), fileMapper);
    }

    public Section getSection(long sectionId) {
        return template.queryForObject(queries.getSectionByIdQuery(), sectionId(sectionId), sectionMapper);
    }

    public List<Section> getSectionSections(long sectionId) {
        return template.query(queries.getSectionSectionsQuery(), sectionId(sectionId), sectionMapper);
    }

    private Map<String, ?> sectionId(long sectionId) {
        return singletonMap(SEC_ID_PARAM, sectionId);
    }
}
