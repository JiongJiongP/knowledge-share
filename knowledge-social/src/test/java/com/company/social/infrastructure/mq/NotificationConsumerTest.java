package com.company.social.infrastructure.mq;

import com.company.social.application.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock private NotificationService notificationService;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks private NotificationConsumer consumer;

    @Test
    void shouldHandleContentPublishedEvent() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
                "type", "CONTENT_PUBLISHED",
                "userId", 1,
                "title", "内容发布成功",
                "content", "《测试》已发布",
                "relatedId", 100,
                "relatedType", "CONTENT"
        ));

        consumer.handleEvent(json);

        verify(notificationService).create(eq(1L), eq("CONTENT_PUBLISHED"),
                eq("内容发布成功"), eq("《测试》已发布"), eq(100L), eq("CONTENT"));
    }

    @Test
    void shouldHandleCommentReplyEvent() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
                "type", "COMMENT_REPLY",
                "userId", 2,
                "title", "有新评论",
                "content", "不错",
                "relatedId", 200,
                "relatedType", "COMMENT"
        ));

        consumer.handleEvent(json);

        verify(notificationService).create(eq(2L), eq("COMMENT_REPLY"),
                eq("有新评论"), eq("不错"), eq(200L), eq("COMMENT"));
    }

    @Test
    void shouldSkipWhenUserIdIsNull() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
                "type", "CONTENT_PUBLISHED",
                "title", "内容发布成功"
        ));

        consumer.handleEvent(json);

        verify(notificationService, never()).create(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldHandleInvalidJson() {
        consumer.handleEvent("not-valid-json");

        verify(notificationService, never()).create(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldHandleLongUserId() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
                "type", "TEST",
                "userId", 999L,
                "title", "t",
                "content", "c",
                "relatedId", 10L,
                "relatedType", "X"
        ));

        consumer.handleEvent(json);

        verify(notificationService).create(eq(999L), eq("TEST"), eq("t"), eq("c"), eq(10L), eq("X"));
    }
}
