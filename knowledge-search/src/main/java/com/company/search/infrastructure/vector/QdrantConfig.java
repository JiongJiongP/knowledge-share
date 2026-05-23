package com.company.search.infrastructure.vector;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QdrantConfig {

    private static final Logger log = LoggerFactory.getLogger(QdrantConfig.class);

    @Value("${qdrant.host:localhost}")
    private String host;

    @Value("${qdrant.port:6334}")
    private int port;

    @Bean
    @ConditionalOnProperty(value = "qdrant.enabled", havingValue = "true", matchIfMissing = true)
    public QdrantClient qdrantClient() {
        try {
            QdrantClient client = new QdrantClient(
                    QdrantGrpcClient.newBuilder(host, port, false).build()
            );
            log.info("Qdrant client connected to {}:{}", host, port);
            return client;
        } catch (Exception e) {
            log.warn("Failed to create Qdrant client: {}", e.getMessage());
            return null;
        }
    }
}
