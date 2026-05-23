package com.company.content.interfaces.controller;

import com.company.common.result.Result;
import com.company.content.application.dto.CreateTagRequest;
import com.company.content.application.dto.TagContentRequest;
import com.company.content.application.service.TagService;
import com.company.content.domain.model.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/api/tags")
    public Result<List<Tag>> listAll() {
        return Result.ok(tagService.listAll());
    }

    @GetMapping("/api/contents/{contentId}/tags")
    public Result<List<Tag>> getContentTags(@PathVariable Long contentId) {
        return Result.ok(tagService.listByContentId(contentId));
    }

    @PostMapping("/api/admin/tags")
    public Result<Tag> create(@Valid @RequestBody CreateTagRequest req, Authentication auth) {
        return Result.ok(tagService.create(req.getName(), req.getColor(), (Long) auth.getPrincipal()));
    }

    @PutMapping("/api/admin/tags/{id}")
    public Result<Tag> update(@PathVariable Long id, @Valid @RequestBody CreateTagRequest req) {
        return Result.ok(tagService.update(id, req.getName(), req.getColor()));
    }

    @DeleteMapping("/api/admin/tags/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.ok(null);
    }

    @PutMapping("/api/contents/{contentId}/tags")
    public Result<Void> setContentTags(@PathVariable Long contentId,
                                       @Valid @RequestBody TagContentRequest req,
                                       Authentication auth) {
        tagService.setContentTags(contentId,
                req.getTagIds() != null ? req.getTagIds() : List.of(),
                (Long) auth.getPrincipal());
        return Result.ok(null);
    }
}
