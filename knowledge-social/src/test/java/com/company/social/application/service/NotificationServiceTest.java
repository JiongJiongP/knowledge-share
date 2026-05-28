package com.company.social.application.service;

import com.company.common.result.PageResult;
import com.company.social.domain.model.Notification;
import com.company.social.domain.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldCreateNotification() {
        notificationService.create(1L, "COMMENT_REPLY", "新回复", "有人回复了你", 100L, "COMMENT");
        verify(notificationRepository).insert(any());
    }

    @Test
    void shouldCountUnread() {
        when(notificationRepository.countUnread(1L)).thenReturn(5L);
        assertThat(notificationService.countUnread(1L)).isEqualTo(5L);
    }

    @Test
    void shouldMarkRead() {
        Notification n = new Notification();
        n.setId(1L);
        n.setUserId(1L);
        when(notificationRepository.findById(1L)).thenReturn(n);
        notificationService.markRead(1L, 1L);
        verify(notificationRepository).markRead(1L);
    }

    @Test
    void shouldMarkAllRead() {
        notificationService.markAllRead(1L);
        verify(notificationRepository).markAllRead(1L);
    }

    @Test
    void shouldDeleteNotification() {
        Notification n = new Notification();
        n.setId(1L);
        n.setUserId(1L);
        when(notificationRepository.findById(1L)).thenReturn(n);
        notificationService.delete(1L, 1L);
        verify(notificationRepository).delete(1L);
    }

    @Test
    void shouldListNotifications() {
        Notification n = new Notification();
        n.setId(1L); n.setUserId(1L); n.setTitle("test");
        when(notificationRepository.findByUser(1L, 1, 10)).thenReturn(List.of(n));
        when(notificationRepository.countByUser(1L)).thenReturn(1L);

        PageResult<Notification> result = notificationService.listByUser(1L, 1, 10);
        assertThat(result.getRecords()).hasSize(1);
    }
}
