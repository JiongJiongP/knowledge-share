package com.company.common.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Sm4Config {

    /** Static key accessible from MyBatis TypeHandlers (which are not Spring beans) */
    private static String dataKey;

    @Value("${sm4.data-key}")
    private String configuredKey;

    @PostConstruct
    public void init() {
        if (configuredKey == null || configuredKey.isBlank()) {
            throw new IllegalStateException("sm4.data-key must be configured in application.yml");
        }
        dataKey = configuredKey;
    }

    public static String getDataKey() {
        if (dataKey == null || dataKey.isBlank()) {
            throw new IllegalStateException("SM4 data key not initialized. Check sm4.data-key configuration.");
        }
        return dataKey;
    }

    /** For unit tests without Spring context. No-op if key already set. */
    public static void initializeForTest(String testKey) {
        if (dataKey == null || dataKey.isBlank()) {
            dataKey = testKey;
        }
    }
}
