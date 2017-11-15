package uk.ac.ebi.biostd.exporter.persistence.dao;

import static java.util.Collections.singletonMap;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.model.Attribute;
import uk.ac.ebi.biostd.exporter.persistence.Queries;
import uk.ac.ebi.biostd.exporter.persistence.mappers.AttributeMapper;

@Component
@AllArgsConstructor
public class FilesDao {

    private final Queries queries;
    private final NamedParameterJdbcTemplate template;
    private final AttributeMapper attributeMapper;

    public List<Attribute> getFilesAttributes(long fileId) {
        return template.query(queries.getFileAttributesQuery(), singletonMap("file_id", fileId), attributeMapper);
    }
}
