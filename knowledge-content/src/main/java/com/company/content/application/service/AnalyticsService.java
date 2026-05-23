package com.company.content.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.content.domain.model.*;
import com.company.content.infrastructure.mapper.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class AnalyticsService {

    private final UserActionLogMapper actionLogMapper;
    private final ContentStatsMapper statsMapper;
    private final SearchHotKeywordMapper hotKeywordMapper;
    private final ContentMapper contentMapper;

    public AnalyticsService(UserActionLogMapper actionLogMapper, ContentStatsMapper statsMapper,
                             SearchHotKeywordMapper hotKeywordMapper, ContentMapper contentMapper) {
        this.actionLogMapper = actionLogMapper;
        this.statsMapper = statsMapper;
        this.hotKeywordMapper = hotKeywordMapper;
        this.contentMapper = contentMapper;
    }

    public Map<String, Object> overview() {
        long totalContents = contentMapper.selectCount(new LambdaQueryWrapper<>());
        long todayContents = contentMapper.selectCount(
            new LambdaQueryWrapper<KnowledgeContent>().ge(KnowledgeContent::getCreatedAt, LocalDate.now()));
        return Map.of("totalContents", totalContents, "todayContents", todayContents);
    }

    public List<Map<String, Object>> contentTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            long count = contentMapper.selectCount(
                new LambdaQueryWrapper<KnowledgeContent>()
                    .ge(KnowledgeContent::getCreatedAt, date.atStartOfDay())
                    .lt(KnowledgeContent::getCreatedAt, date.plusDays(1).atStartOfDay()));
            trend.add(Map.of("date", date.toString(), "count", count));
        }
        return trend;
    }

    public List<ContentStats> hotContent() {
        return statsMapper.selectList(
            new LambdaQueryWrapper<ContentStats>()
                .orderByDesc(ContentStats::getViewCount)
                .last("LIMIT 20"));
    }

    public List<SearchHotKeyword> hotKeywords() {
        return hotKeywordMapper.selectList(
            new LambdaQueryWrapper<SearchHotKeyword>()
                .eq(SearchHotKeyword::getStatDate, LocalDate.now())
                .orderByDesc(SearchHotKeyword::getSearchCount)
                .last("LIMIT 20"));
    }

    public void logAction(Long userId, String actionType, String targetType, Long targetId) {
        UserActionLog log = new UserActionLog();
        log.setUserId(userId);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        actionLogMapper.insert(log);
    }
}
