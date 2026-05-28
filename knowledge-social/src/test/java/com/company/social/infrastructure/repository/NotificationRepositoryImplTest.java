package com.company.social.infrastructure.repository;

import com.company.social.domain.model.Notification;
import com.company.social.infrastructure.mapper.NotificationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRepositoryImplTest {

    @Mock private NotificationMapper mapper;
    @InjectMocks private NotificationRepositoryImpl repo;

    @Test
    void shouldInsertNotification() {
        Notification n = new Notification();
        when(mapper.insert(any())).thenReturn(1);

        repo.insert(n);
        verify(mapper).insert(n);
    }

    @Test
    void shouldFindById() {
        Notification n = new Notification();
        n.setId(1L);
        when(mapper.selectById(1L)).thenReturn(n);

        Notification result = repo.findById(1L);
        assertThat(result).isNotNull();
    }

    @Test
    void shouldMarkRead() {
        when(mapper.updateById(any())).thenReturn(1);

        repo.markRead(1L);
        verify(mapper).updateById(argThat(n -> n.getIsRead() == 1));
    }

    @Test
    void shouldMarkAllRead() {
        when(mapper.update(any(Notification.class), any())).thenReturn(5);

        repo.markAllRead(1L);
        verify(mapper).update(any(Notification.class), any());
    }

    @Test
    void shouldDeleteNotification() {
        when(mapper.deleteById(1L)).thenReturn(1);

        repo.delete(1L);
        verify(mapper).deleteById(1L);
    }

    @Test
    void shouldCountUnread() {
        when(mapper.selectCount(any())).thenReturn(3L);

        long count = repo.countUnread(1L);
        assertThat(count).isEqualTo(3L);
    }

    @Test
    void shouldCountByUser() {
        when(mapper.selectCount(any())).thenReturn(10L);

        long count = repo.countByUser(1L);
        assertThat(count).isEqualTo(10L);
    }
}
