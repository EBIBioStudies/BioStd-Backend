package uk.ac.ebi.biostd.exporter.persistence.mappers;


import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.model.Section;

@Component
public class SectionMapper implements RowMapper<Section> {

    @Override
    public Section mapRow(ResultSet rs, int rowNum) throws SQLException {
        Section section = new Section();
        section.setId(rs.getLong("id"));
        section.setAccNo(rs.getString("accno"));
        section.setType(rs.getString("type"));
        section.setFileList(rs.getString("fileList"));
        return section;
    }
}
