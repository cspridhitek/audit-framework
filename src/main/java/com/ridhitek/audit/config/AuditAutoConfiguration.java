package com.ridhitek.audit.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@AutoConfiguration
@ComponentScan(basePackages = "com.ridhitek.audit")  // Ensures Audit beans are registered
@EnableJpaRepositories(
        basePackages = "com.audit.repository",
        entityManagerFactoryRef = "auditEntityManagerFactory",
        transactionManagerRef = "auditTransactionManager"
)
public class AuditAutoConfiguration {
}

