package com.company.social.infrastructure.mq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private EventPublisher publisher;

    @Test
    void shouldPublishContentPublishedEvent() {
        publisher.publishContentPublished(1L, "测试标题", 100L);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMqConfig.EXCHANGE),
                eq("notification.content"), any(Map.class));
    }

    @Test
    void shouldPublishCommentCreatedEvent() {
        publisher.publishCommentCreated(1L, 2L, 100L, "评论内容");

        verify(rabbitTemplate).convertAndSend(eq(RabbitMqConfig.EXCHANGE),
                eq("notification.comment"), any(Map.class));
    }

    @Test
    void shouldPublishGroupJoinRequestEvent() {
        publisher.publishGroupJoinRequest(1L, 2L, "张三", 3L);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMqConfig.EXCHANGE),
                eq("notification.group"), any(Map.class));
    }

    @Test
    void shouldHandlePublishFailure() {
        doThrow(new RuntimeException("Connection refused"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        publisher.publish("test.key", Map.of("type", "TEST"));

        verify(rabbitTemplate).convertAndSend(eq(RabbitMqConfig.EXCHANGE),
                eq("test.key"), any(Object.class));
    }

    @Test
    void shouldIncludeCorrectFieldsInContentEvent() {
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);

        publisher.publishContentPublished(42L, "标题", 7L);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMqConfig.EXCHANGE),
                eq("notification.content"), captor.capture());

        Map<String, Object> event = captor.getValue();
        assertThat(event.get("type")).isEqualTo("CONTENT_PUBLISHED");
        assertThat(event.get("userId")).isEqualTo(7L);
        assertThat(event.get("relatedId")).isEqualTo(42L);
        assertThat(event.get("relatedType")).isEqualTo("CONTENT");
    }
}
