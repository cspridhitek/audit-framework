package com.ridhitek.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = KafkaAutoConfiguration.class)
@EnableAsync
public class AuditFrameworkApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuditFrameworkApplication.class, args);
    }
}