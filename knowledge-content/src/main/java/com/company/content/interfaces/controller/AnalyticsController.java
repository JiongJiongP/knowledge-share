package com.company.content.interfaces.controller;

import com.company.common.result.Result;
import com.company.content.application.service.AnalyticsService;
import com.company.content.domain.model.ContentStats;
import com.company.content.domain.model.SearchHotKeyword;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(analyticsService.overview());
    }

    @GetMapping("/content-trend")
    public Result<List<Map<String, Object>>> contentTrend() {
        return Result.ok(analyticsService.contentTrend());
    }

    @GetMapping("/hot-content")
    public Result<List<ContentStats>> hotContent() {
        return Result.ok(analyticsService.hotContent());
    }

    @GetMapping("/hot-keywords")
    public Result<List<SearchHotKeyword>> hotKeywords() {
        return Result.ok(analyticsService.hotKeywords());
    }
}
