package com.ridhitek.audit.config;


import com.ridhitek.audit.audit.AuditInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class HibernateConfig {

    private final AuditInterceptor auditInterceptor;

    @Autowired
    public HibernateConfig(AuditInterceptor auditInterceptor) {
        this.auditInterceptor = auditInterceptor;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setPackagesToScan("com.ridhitek.audit"); // Change to your package

        // Set Hibernate properties and Interceptor
        Properties properties = new Properties();
        properties.put("hibernate.session_factory.interceptor", auditInterceptor);
        em.setJpaProperties(properties);

        return em;
    }
}


