package com.ridhitek.audit.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.Properties;

@Configuration
@EntityScan(basePackages = "com.ridhitek.audit.entity") // Scan only Audit entities
@EnableJpaRepositories(
        basePackages = "com.ridhitek.audit.repository",
        entityManagerFactoryRef = "auditEntityManagerFactory",
        transactionManagerRef = "auditTransactionManager"
)
public class AuditDatabaseConfig {

    @Bean
    @ConfigurationProperties("audit.datasource") // Reads from application.properties
    public DataSourceProperties auditDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "auditDataSource")
    public DataSource auditDataSource() {
        return auditDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "auditEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean auditEntityManagerFactory(
            @Qualifier("auditDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setPackagesToScan("com.ridhitek.audit.entity");  // Ensure only Audit entities are scanned

        // Hibernate properties
        Properties properties = new Properties();
        properties.put("hibernate.hbm2ddl.auto", "update"); // Update schema automatically
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        em.setJpaProperties(properties);

        return em;
    }

    @Bean(name = "auditTransactionManager")
    public JpaTransactionManager auditTransactionManager(
            @Qualifier("auditEntityManagerFactory") LocalContainerEntityManagerFactoryBean auditEntityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(Objects.requireNonNull(auditEntityManagerFactory.getObject()));
        return transactionManager;
    }
}

