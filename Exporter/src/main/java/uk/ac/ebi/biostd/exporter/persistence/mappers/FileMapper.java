package uk.ac.ebi.biostd.exporter.persistence.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.model.File;

@Component
public class FileMapper implements RowMapper<File> {

    @Override
    public File mapRow(ResultSet rs, int rowNum) throws SQLException {
        File file = new File();
        file.setId(rs.getLong("id"));
        file.setPath(rs.getString("path"));
        file.setName(rs.getString("name"));
        file.setSize(rs.getLong("size"));
        file.setDirectory(rs.getBoolean("directory"));
        return file;
    }
}
