package uk.ac.ebi.biostd.webapp.application.configuration;

import java.util.Properties;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
public class DbConfiguration {

    @Bean
    public DataSource source(ConfigProperties legacyProperties) {
        DataSource dataSource = new DataSource();
        dataSource.setDriverClassName(legacyProperties.get("db.hibernate.connection.driver_class"));
        dataSource.setUrl(legacyProperties.get("db.hibernate.connection.url"));
        dataSource.setUsername(legacyProperties.get("db.hibernate.connection.username"));
        dataSource.setPassword(legacyProperties.get("db.hibernate.connection.password"));
        dataSource.setTestWhileIdle(true);
        dataSource.setValidationQuery("SELECT 1");
        return dataSource;
    }

    @Bean
    public PhysicalNamingStrategy physicalNamingStrategy() {
        return new PhysicalNamingStrategyStandardImpl();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            ConfigProperties properties, DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource);
        entityManagerFactory.setPackagesToScan("uk.ac.ebi.biostd.webapp.application.persitence.entities");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactory.setJpaVendorAdapter(vendorAdapter);

        Properties additionalProperties = new Properties();
        additionalProperties.put("hibernate.dialect", properties.get("db.hibernate.dialect"));
        additionalProperties.put("hibernate.show_sql", false);
        additionalProperties.put("hibernate.hbm2ddl.auto", "none");
        entityManagerFactory.setJpaProperties(additionalProperties);
        return entityManagerFactory;
    }
}
