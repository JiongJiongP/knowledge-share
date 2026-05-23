package com.company.social.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.social.domain.model.Notification;
import com.company.social.domain.repository.NotificationRepository;
import com.company.social.infrastructure.mapper.NotificationMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationMapper mapper;

    public NotificationRepositoryImpl(NotificationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void insert(Notification notification) {
        mapper.insert(notification);
    }

    @Override
    public List<Notification> findByUser(Long userId, int page, int size) {
        return mapper.selectPage(
            new Page<>(page, size, false),
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt)
        ).getRecords();
    }

    @Override
    public long countByUser(Long userId) {
        return mapper.selectCount(
            new LambdaQueryWrapper<Notification>().eq(Notification::getUserId, userId)
        );
    }

    @Override
    public long countUnread(Long userId) {
        return mapper.selectCount(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
        );
    }

    @Override
    public void markRead(Long id) {
        Notification n = new Notification();
        n.setId(id);
        n.setIsRead(1);
        mapper.updateById(n);
    }

    @Override
    public void markAllRead(Long userId) {
        Notification n = new Notification();
        n.setIsRead(1);
        mapper.update(n,
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
        );
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
