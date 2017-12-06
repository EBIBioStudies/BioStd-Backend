package uk.ac.ebi.biostd.exporter.persistence.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.model.Attribute;

@Component
public class AttributeMapper implements RowMapper<Attribute> {

    @Override
    public Attribute mapRow(ResultSet rs, int rowNum) throws SQLException {
        Attribute attribute = new Attribute();
        attribute.setName(rs.getString("name"));
        attribute.setValue(rs.getString("value"));
        attribute.setValueQualifierString(rs.getString("valueQualifierString"));
        attribute.setReference(rs.getBoolean("reference"));
        return attribute;
    }
}
