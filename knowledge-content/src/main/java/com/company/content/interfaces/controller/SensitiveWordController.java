package com.company.content.interfaces.controller;

import com.company.common.result.Result;
import com.company.content.application.service.SensitiveWordService;
import com.company.content.domain.model.SensitiveWord;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sensitive-words")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    public SensitiveWordController(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    @GetMapping
    public Result<List<SensitiveWord>> listAll() {
        return Result.ok(sensitiveWordService.listAll());
    }

    @PostMapping
    public Result<SensitiveWord> add(@RequestBody Map<String, String> body) {
        return Result.ok(sensitiveWordService.add(
                body.get("word"), body.get("category")));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sensitiveWordService.delete(id);
        return Result.ok(null);
    }

    @PostMapping("/batch")
    public Result<Map<String, Object>> batchImport(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> words = (List<String>) body.get("words");
        String category = (String) body.getOrDefault("category", "GENERAL");
        int count = sensitiveWordService.batchImport(words, category);
        return Result.ok(Map.of("imported", count));
    }

    @PostMapping("/check")
    public Result<Map<String, Object>> check(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        boolean hasSensitive = sensitiveWordService.containsSensitiveWord(text);
        return Result.ok(Map.of("containsSensitiveWord", hasSensitive));
    }
}
