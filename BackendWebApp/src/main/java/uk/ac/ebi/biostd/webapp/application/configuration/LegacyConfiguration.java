package uk.ac.ebi.biostd.webapp.application.configuration;

import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.mng.SessionManager;

@Configuration
public class LegacyConfiguration {

    @Autowired
    private Environment env;

    @Bean
    public SessionManager serviceManager() {
        return BackendConfig.getServiceManager().getSessionManager();
    }

    @Bean
    public DataSource source() {
        Map<String, Object> dbConfig = BackendConfig.getDatabaseConfig();
        return DataSourceBuilder.create()
                .username(dbConfig.get("hibernate.connection.username").toString())
                .password(dbConfig.get("hibernate.connection.password").toString())
                .url(dbConfig.get("hibernate.connection.url").toString())
                .driverClassName(dbConfig.get("hibernate.connection.driver_class").toString())
                .build();
    }

    @Bean
    public PhysicalNamingStrategy physicalNamingStrategy() {
        return new PhysicalNamingStrategyStandardImpl();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        Map<String, Object> dbConfig = BackendConfig.getDatabaseConfig();

        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource);
        entityManagerFactory.setPackagesToScan("uk.ac.ebi.biostd.webapp.application.persitence.entities");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactory.setJpaVendorAdapter(vendorAdapter);

        Properties additionalProperties = new Properties();
        additionalProperties.put("hibernate.dialect", dbConfig.get("hibernate.dialect"));
        additionalProperties.put("hibernate.show_sql", false);
        additionalProperties.put("hibernate.hbm2ddl.auto", "none");
        entityManagerFactory.setJpaProperties(additionalProperties);
        return entityManagerFactory;
    }
}
