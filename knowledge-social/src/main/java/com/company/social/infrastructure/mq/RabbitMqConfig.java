package com.company.social.infrastructure.mq;

import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMqConfig {

    public static final String EXCHANGE = "knowledge.events";
    public static final String DLX = "knowledge.events.dlx";

    // Queue names
    public static final String QUEUE_NOTIFICATION = "notification.queue";
    public static final String QUEUE_SEARCH_INDEX = "search.index.queue";
    public static final String QUEUE_ANALYTICS = "analytics.queue";
    public static final String DLQ = "dead.letter.queue";

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey("dead.notification")
                .build();
    }

    @Bean
    public Queue searchIndexQueue() {
        return QueueBuilder.durable(QUEUE_SEARCH_INDEX)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey("dead.search")
                .build();
    }

    @Bean
    public Queue analyticsQueue() {
        return QueueBuilder.durable(QUEUE_ANALYTICS)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey("dead.analytics")
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(eventExchange()).with("notification.#");
    }

    @Bean
    public Binding searchBinding() {
        return BindingBuilder.bind(searchIndexQueue()).to(eventExchange()).with("search.#");
    }

    @Bean
    public Binding analyticsBinding() {
        return BindingBuilder.bind(analyticsQueue()).to(eventExchange()).with("analytics.#");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("dead.#");
    }
}
