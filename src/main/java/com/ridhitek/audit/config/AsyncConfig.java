package com.ridhitek.audit.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableConfigurationProperties(AuditProperties.class)
@Validated
public class AsyncConfig {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // Set the core pool size
        executor.setMaxPoolSize(50); // Set the maximum pool size
        executor.setQueueCapacity(100); // Set the queue capacity
        executor.setThreadNamePrefix("Async-"); // Set the thread name prefix
        executor.setWaitForTasksToCompleteOnShutdown(true); // Wait for tasks to complete on shutdown
        executor.setAwaitTerminationSeconds(30); // Set the await termination seconds
        executor.initialize();
        return executor;
    }
}
