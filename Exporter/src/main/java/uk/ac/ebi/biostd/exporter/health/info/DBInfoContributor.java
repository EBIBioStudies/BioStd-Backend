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
        Map<String, Object> dbDetails = new HashMap<>();
        dbDetails.put("active", poolMetadata.getActive());
        dbDetails.put("max", poolMetadata.getMax());
        dbDetails.put("min", poolMetadata.getMin());
        dbDetails.put("url", dataSource.getJdbcUrl());
        dbDetails.put("idleTimeOut", dataSource.getIdleTimeout());
        dbDetails.put("connectionTimeOut", dataSource.getConnectionTimeout());
        builder.withDetail("dbPool", dbDetails);
    }
}
