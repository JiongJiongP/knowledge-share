package com.company.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.company.search.infrastructure.elasticsearch.ElasticsearchConfig;
import com.company.search.infrastructure.vector.QdrantConfig;
import io.qdrant.client.QdrantClient;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import static org.mockito.Mockito.mock;
import org.mockito.Mockito;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.company.search",
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ElasticsearchConfig.class),
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = QdrantConfig.class)
        })
public class TestConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        return mock(ElasticsearchClient.class, Mockito.RETURNS_MOCKS);
    }

    @Bean
    public QdrantClient qdrantClient() {
        return mock(QdrantClient.class, Mockito.RETURNS_MOCKS);
    }
}
