package uk.ac.ebi.biostd.exporter.persistence.dao;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import uk.ac.ebi.biostd.exporter.persistence.Queries;
import uk.ac.ebi.biostd.exporter.persistence.dao.PersistenceTest.TestConfig;
import uk.ac.ebi.biostd.exporter.test.IgnoreDuringScan;

@ContextConfiguration(classes = TestConfig.class)
public class PersistenceTest {

    @IgnoreDuringScan
    @Configuration
    @ComponentScan(basePackageClasses = {SubmissionDao.class, Queries.class})
    @EnableAutoConfiguration
    public static class TestConfig {

        @Bean
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                    //  .setName("biostd_test;DATABASE_TO_UPPER=false;MODE=MYSQL")
                    .setType(EmbeddedDatabaseType.H2)
                    .addScript("classpath:scripts/sql/create_schema.sql")
                    .addScript("classpath:scripts/sql/init-full-export.sql")
                    .addScript("classpath:persistence_dao/release_feature.sql")
                    .build();
        }
    }
}
