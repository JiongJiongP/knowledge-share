package com.company.social.application.service;

import com.company.common.result.PageResult;
import com.company.social.domain.model.Notification;
import com.company.social.domain.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void create(Long userId, String type, String title, String content,
                       Long relatedId, String relatedType) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedId(relatedId);
        n.setRelatedType(relatedType);
        n.setIsRead(0);
        notificationRepository.insert(n);
    }

    public PageResult<Notification> listByUser(Long userId, int page, int size) {
        return PageResult.of(
                notificationRepository.findByUser(userId, page, size),
                notificationRepository.countByUser(userId),
                page, size
        );
    }

    public long countUnread(Long userId) {
        return notificationRepository.countUnread(userId);
    }

    @Transactional
    public void markRead(Long id) {
        notificationRepository.markRead(id);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId);
    }

    @Transactional
    public void delete(Long id) {
        notificationRepository.delete(id);
    }
}
