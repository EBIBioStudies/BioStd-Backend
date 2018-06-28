package uk.ac.ebi.biostd.exporter.persistence.dao;

import java.util.Collections;
import java.util.List;
import lombok.Data;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biostd.exporter.persistence.Queries;
import uk.ac.ebi.biostd.exporter.persistence.model.UserDropboxInfo;

@Data
@Component
public class UsersDao {

    private final NamedParameterJdbcTemplate template;
    private final Queries queries;

    public List<UserDropboxInfo> getUsersDropbox() {
        return template.query(
                queries.getUserDropboxes(), Collections.emptyMap(), new BeanPropertyRowMapper<>(UserDropboxInfo.class));
    }
}
