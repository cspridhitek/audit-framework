package com.ridhitek.audit.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.annotation.Validated;

@Configuration
@EnableAsync
@EnableConfigurationProperties(AuditProperties.class)
@Validated
public class AsyncConfig {
}
