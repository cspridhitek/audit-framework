package com.ridhitek.audit.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class EnvConfig {

    @Autowired
    private Environment env;

    public String getDbUser() {
        return env.getProperty("AUDIT_DB_USER", "root");
    }

    public String getDbPassword() {
        return env.getProperty("AUDIT_DB_PASSWORD", "root");
    }
}
