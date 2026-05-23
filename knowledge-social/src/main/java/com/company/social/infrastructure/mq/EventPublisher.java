package com.company.social.infrastructure.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnBean(RabbitTemplate.class)
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(String routingKey, Map<String, Object> event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, routingKey, event);
            log.debug("Event published: routingKey={}", routingKey);
        } catch (Exception e) {
            log.warn("Failed to publish event: {}", e.getMessage());
        }
    }

    public void publishContentPublished(Long contentId, String title, Long authorId) {
        publish("notification.content", Map.of(
                "type", "CONTENT_PUBLISHED",
                "userId", authorId,
                "title", "内容发布成功",
                "content", "《" + title + "》已发布",
                "relatedId", contentId,
                "relatedType", "CONTENT"
        ));
    }

    public void publishCommentCreated(Long commentId, Long contentId, Long authorId, String excerpt) {
        publish("notification.comment", Map.of(
                "type", "COMMENT_REPLY",
                "userId", authorId,
                "title", "有新评论",
                "content", excerpt,
                "relatedId", commentId,
                "relatedType", "COMMENT"
        ));
    }

    public void publishGroupJoinRequest(Long groupId, Long applicantId, Long ownerId) {
        publish("notification.group", Map.of(
                "type", "GROUP_JOIN_APPLY",
                "userId", ownerId,
                "title", "新的入群申请",
                "content", "用户 " + applicantId + " 申请加入群组",
                "relatedId", groupId,
                "relatedType", "GROUP_APPLICATION"
        ));
    }
}
