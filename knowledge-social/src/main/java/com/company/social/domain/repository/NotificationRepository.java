package com.company.social.domain.repository;

import com.company.social.domain.model.Notification;

import java.util.List;

public interface NotificationRepository {
    void insert(Notification notification);
    List<Notification> findByUser(Long userId, int page, int size);
    long countByUser(Long userId);
    long countUnread(Long userId);
    void markRead(Long id);
    void markAllRead(Long userId);
    void delete(Long id);
}
