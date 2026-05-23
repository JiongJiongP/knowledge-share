package com.company.search.interfaces.controller;

import com.company.common.result.PageResult;
import com.company.common.result.Result;
import com.company.search.application.dto.SearchResult;
import com.company.search.application.service.SearchService;
import com.company.search.application.service.VectorSearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;
    private final VectorSearchService vectorSearchService;

    public SearchController(SearchService searchService, VectorSearchService vectorSearchService) {
        this.searchService = searchService;
        this.vectorSearchService = vectorSearchService;
    }

    @GetMapping
    public Result<PageResult<SearchResult>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "relevance") String sort) {

        List<SearchResult> results = searchService.search(keyword, page, size, sort);
        return Result.ok(PageResult.of(results, results.size(), page, size));
    }

    @GetMapping("/vector")
    public Result<Map<String, Object>> vectorSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int topK) {

        List<Long> ids = vectorSearchService.search(q, topK);
        return Result.ok(Map.of("ids", ids, "total", ids.size()));
    }

    @GetMapping("/hybrid")
    public Result<PageResult<SearchResult>> hybridSearch(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<SearchResult> results = searchService.hybridSearch(keyword, page, size);
        return Result.ok(PageResult.of(results, results.size(), page, size));
    }
}
