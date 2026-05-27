package com.company.social.interfaces.controller;

import com.company.common.result.PageResult;
import com.company.common.result.Result;
import com.company.social.application.service.NotificationService;
import com.company.social.domain.model.Notification;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Result<PageResult<Notification>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        return Result.ok(notificationService.listByUser((Long) auth.getPrincipal(), page, size));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount(Authentication auth) {
        long count = notificationService.countUnread((Long) auth.getPrincipal());
        return Result.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, Authentication auth) {
        notificationService.markRead(id, (Long) auth.getPrincipal());
        return Result.ok(null);
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead(Authentication auth) {
        notificationService.markAllRead((Long) auth.getPrincipal());
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        notificationService.delete(id, (Long) auth.getPrincipal());
        return Result.ok(null);
    }
}
