package com.company.search.infrastructure.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String uris;

    @Bean
    @ConditionalOnProperty(value = "elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
    public ElasticsearchClient elasticsearchClient() {
        String host = uris.replace("http://", "").replace("https://", "");
        String[] parts = host.split(":");
        String hostname = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;

        RestClient restClient = RestClient.builder(
                new HttpHost(hostname, port, "http")
        ).setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(1000)    // 1 秒连接超时，ES 不可用时快速失败
                        .setSocketTimeout(2000)     // 2 秒读取超时
        ).build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper()
        );

        return new ElasticsearchClient(transport);
    }
}
