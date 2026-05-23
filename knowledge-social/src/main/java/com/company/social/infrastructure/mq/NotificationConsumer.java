package com.company.social.infrastructure.mq;

import com.company.social.application.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnBean(RabbitTemplate.class)
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_NOTIFICATION)
    public void handleEvent(String messageJson) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(messageJson, Map.class);
            String type = (String) event.get("type");
            Long userId = toLong(event.get("userId"));
            String title = (String) event.get("title");
            String content = (String) event.get("content");
            Long relatedId = toLong(event.get("relatedId"));
            String relatedType = (String) event.get("relatedType");

            if (userId != null) {
                notificationService.create(userId, type, title, content, relatedId, relatedType);
            }
        } catch (Exception e) {
            log.warn("Failed to process notification event: {}", e.getMessage());
        }
    }

    private Long toLong(Object val) {
        if (val instanceof Integer i) return i.longValue();
        if (val instanceof Long l) return l;
        return null;
    }
}
