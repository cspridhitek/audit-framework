package com.ridhitek.audit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:application-audit.properties", ignoreResourceNotFound = false)
public class AuditFrameworkPropertyConfig {
    public AuditFrameworkPropertyConfig() {
        System.out.println("✅ audit-framework properties loaded into environment!");
    }
}
