package com.company.content.application.service;

import com.company.content.domain.model.ContentStats;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.SearchHotKeyword;
import com.company.content.domain.model.UserActionLog;
import com.company.content.infrastructure.mapper.ContentMapper;
import com.company.content.infrastructure.mapper.ContentStatsMapper;
import com.company.content.infrastructure.mapper.SearchHotKeywordMapper;
import com.company.content.infrastructure.mapper.UserActionLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private UserActionLogMapper actionLogMapper;

    @Mock
    private ContentStatsMapper statsMapper;

    @Mock
    private SearchHotKeywordMapper hotKeywordMapper;

    @Mock
    private ContentMapper contentMapper;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void shouldReturnOverview() {
        when(contentMapper.selectCount(any())).thenReturn(100L);

        Map<String, Object> overview = analyticsService.overview();

        assertThat(overview).containsKey("totalContents");
        assertThat(overview).containsKey("todayContents");
        assertThat(overview.get("totalContents")).isEqualTo(100L);
    }

    @Test
    void shouldReturnContentTrend() {
        when(contentMapper.selectCount(any())).thenReturn(5L);

        List<Map<String, Object>> trend = analyticsService.contentTrend();

        assertThat(trend).hasSize(7);
        assertThat(trend.get(0)).containsKey("date");
        assertThat(trend.get(0)).containsKey("count");
    }

    @Test
    void shouldReturnHotContent() {
        ContentStats stats = new ContentStats();
        stats.setContentId(1L);
        stats.setViewCount(100L);
        when(statsMapper.selectList(any())).thenReturn(List.of(stats));

        List<ContentStats> result = analyticsService.hotContent();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getViewCount()).isEqualTo(100L);
    }

    @Test
    void shouldReturnHotKeywords() {
        SearchHotKeyword keyword = new SearchHotKeyword();
        keyword.setKeyword("测试");
        keyword.setSearchCount(50);
        when(hotKeywordMapper.selectList(any())).thenReturn(List.of(keyword));

        List<SearchHotKeyword> result = analyticsService.hotKeywords();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKeyword()).isEqualTo("测试");
    }

    @Test
    void shouldLogAction() {
        when(actionLogMapper.insert(any())).thenReturn(1);

        analyticsService.logAction(1L, "VIEW", "CONTENT", 100L);

        verify(actionLogMapper).insert(any(UserActionLog.class));
    }
}
