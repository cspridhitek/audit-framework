package com.ridhitek.audit.config;

import com.ridhitek.audit.audit.AuditInterceptor;
import com.ridhitek.audit.repository.AuditLogRepository;
import com.ridhitek.audit.producer.AuditLogProducer;
import com.ridhitek.audit.config.AuditProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EntityScan(basePackages = "com.ridhitek.audit.entity") // Scan only Audit entities
@EnableJpaRepositories(
        basePackages = "com.ridhitek.audit.repository",
        entityManagerFactoryRef = "auditEntityManagerFactory",
        transactionManagerRef = "auditTransactionManager"
)
public class AuditAutoConfiguration {
    @Bean
    public AuditInterceptor auditInterceptor(AuditLogRepository auditLogRepository, AuditLogProducer auditLogProducer, AuditProperties auditProperties) {
        return new AuditInterceptor(auditLogRepository, auditLogProducer, auditProperties);
    }
}

