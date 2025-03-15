package com.ridhitek.audit.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "audit")
public class AuditProperties {

    @NotBlank(message = "Handler type must not be blank")
    @Pattern(regexp = "database|kafka_database", message = "Handler type must be either 'database' or 'kafka_database'")
    private String handlerType;

    public String getHandlerType() {
        return handlerType;
    }

    public void setHandlerType(String handlerType) {
        this.handlerType = handlerType;
    }
}