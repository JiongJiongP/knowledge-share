package com.company.content.interfaces.controller;

import com.company.common.exception.GlobalExceptionHandler;
import com.company.content.application.service.AnalyticsService;
import com.company.content.domain.model.ContentStats;
import com.company.content.domain.model.SearchHotKeyword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldGetOverview() throws Exception {
        when(analyticsService.overview()).thenReturn(Map.of("totalContent", 100, "totalViews", 5000L));

        mockMvc.perform(get("/api/admin/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalContent").value(100));
    }

    @Test
    void shouldGetContentTrend() throws Exception {
        when(analyticsService.contentTrend()).thenReturn(List.of(Map.of("date", "2026-05-01", "count", 10)));

        mockMvc.perform(get("/api/admin/analytics/content-trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].date").value("2026-05-01"));
    }

    @Test
    void shouldGetHotContent() throws Exception {
        ContentStats cs = new ContentStats();
        cs.setId(1L);
        cs.setContentId(10L);
        cs.setViewCount(500L);
        when(analyticsService.hotContent()).thenReturn(List.of(cs));

        mockMvc.perform(get("/api/admin/analytics/hot-content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].viewCount").value(500));
    }

    @Test
    void shouldGetHotKeywords() throws Exception {
        SearchHotKeyword kw = new SearchHotKeyword();
        kw.setId(1L);
        kw.setKeyword("Java");
        kw.setSearchCount(100);
        when(analyticsService.hotKeywords()).thenReturn(List.of(kw));

        mockMvc.perform(get("/api/admin/analytics/hot-keywords"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].keyword").value("Java"));
    }
}
