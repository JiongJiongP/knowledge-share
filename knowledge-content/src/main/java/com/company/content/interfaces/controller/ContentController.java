package com.company.content.interfaces.controller;

import com.company.common.result.PageResult;
import com.company.common.result.Result;
import com.company.content.application.dto.ContentListQuery;
import com.company.content.application.dto.CreateContentRequest;
import com.company.content.application.service.ContentService;
import com.company.content.domain.model.KnowledgeContent;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contents")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping
    public Result<PageResult<KnowledgeContent>> list(@Valid ContentListQuery query) {
        return Result.ok(contentService.listPublished(
                query.getPage(), query.getSize(), query.getSort(),
                query.getContentType(), query.getKeyword()));
    }

    @GetMapping("/{id}")
    public Result<KnowledgeContent> get(@PathVariable Long id, Authentication auth) {
        return Result.ok(contentService.getAccessible(id, (Long) auth.getPrincipal()));
    }

    @PostMapping
    public Result<KnowledgeContent> create(@Valid @RequestBody CreateContentRequest req,
                                           Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(contentService.create(userId, req));
    }

    @PutMapping("/{id}")
    public Result<KnowledgeContent> update(@PathVariable Long id,
                                           @Valid @RequestBody CreateContentRequest req,
                                           Authentication auth) {
        return Result.ok(contentService.update(id, (Long) auth.getPrincipal(), req));
    }

    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id, Authentication auth) {
        contentService.publish(id, (Long) auth.getPrincipal());
        return Result.ok(null);
    }

    @PostMapping("/{id}/draft")
    public Result<Void> saveDraft(@PathVariable Long id,
                                  @Valid @RequestBody CreateContentRequest req,
                                  Authentication auth) {
        contentService.saveDraft(id, (Long) auth.getPrincipal(), req);
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        contentService.softDelete(id, (Long) auth.getPrincipal());
        return Result.ok(null);
    }

    @PostMapping("/reindex")
    public Result<Map<String, Object>> reindex() {
        contentService.reindexAllPublishedAsync();
        return Result.ok(Map.of("status", "started", "message", "全量索引同步已在后台启动，查看应用日志跟踪进度"));
    }
}
