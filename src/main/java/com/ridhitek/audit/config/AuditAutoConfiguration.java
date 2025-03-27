package com.ridhitek.audit.config;

import com.ridhitek.audit.audit.AuditInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ComponentScan(basePackages = "com.ridhitek.audit")  // Ensures Audit beans are registered
@EnableJpaRepositories(
        basePackages = "com.audit.repository",
        entityManagerFactoryRef = "auditEntityManagerFactory",
        transactionManagerRef = "auditTransactionManager"
)
@EnableConfigurationProperties
public class AuditAutoConfiguration {

    @Bean
    public  AuditInterceptor auditInterceptor(ApplicationContext context){
        return new AuditInterceptor(context);
    }
}

