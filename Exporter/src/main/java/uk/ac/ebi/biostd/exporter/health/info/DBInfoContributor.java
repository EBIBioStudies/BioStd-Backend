package uk.ac.ebi.biostd.exporter.health.info;

import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.jdbc.metadata.HikariDataSourcePoolMetadata;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DBInfoContributor implements InfoContributor {

    private final HikariDataSourcePoolMetadata poolMetadata;
    private final HikariDataSource dataSource;

    @Override
    public void contribute(Builder builder) {
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("active", poolMetadata.getActive());
        userDetails.put("max", poolMetadata.getMax());
        userDetails.put("min", poolMetadata.getMin());
        userDetails.put("url", dataSource.getJdbcUrl());
        userDetails.put("idleTimeOut", dataSource.getIdleTimeout());
        userDetails.put("connectionTimeOut", dataSource.getConnectionTimeout());
        builder.withDetail("dbPool", userDetails);
    }
}
