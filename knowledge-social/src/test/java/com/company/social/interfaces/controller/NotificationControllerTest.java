package com.company.social.interfaces.controller;

import com.company.common.exception.GlobalExceptionHandler;
import com.company.common.result.PageResult;
import com.company.social.application.service.NotificationService;
import com.company.social.domain.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldListNotifications() throws Exception {
        Notification n = new Notification();
        n.setId(1L);
        n.setContent("新消息");
        PageResult<Notification> page = PageResult.of(List.of(n), 1L, 1, 10);
        when(notificationService.listByUser(1L, 1, 10)).thenReturn(page);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(get("/api/notifications?page=1&size=10")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].content").value("新消息"));
    }

    @Test
    void shouldGetUnreadCount() throws Exception {
        when(notificationService.countUnread(1L)).thenReturn(5L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.count").value(5));
    }

    @Test
    void shouldMarkRead() throws Exception {
        doNothing().when(notificationService).markRead(1L, 1L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(put("/api/notifications/1/read")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldMarkAllRead() throws Exception {
        doNothing().when(notificationService).markAllRead(1L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(put("/api/notifications/read-all")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldDeleteNotification() throws Exception {
        doNothing().when(notificationService).delete(1L, 1L);

        var auth = new UsernamePasswordAuthenticationToken(1L, null);

        mockMvc.perform(delete("/api/notifications/1")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
