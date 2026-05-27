package com.company.content.interfaces.controller;

import com.company.common.result.Result;
import com.company.content.application.dto.SensitiveWordBatchRequest;
import com.company.content.application.dto.SensitiveWordCheckRequest;
import com.company.content.application.dto.SensitiveWordRequest;
import com.company.content.application.service.SensitiveWordService;
import com.company.content.domain.model.SensitiveWord;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Result<Void> add(@Valid @RequestBody SensitiveWordRequest req) {
        sensitiveWordService.add(req.getWord(), req.getCategory());
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sensitiveWordService.delete(id);
        return Result.ok(null);
    }

    @PostMapping("/batch")
    public Result<Integer> batchImport(@Valid @RequestBody SensitiveWordBatchRequest req) {
        return Result.ok(sensitiveWordService.batchImport(req.getWords(), req.getCategory()));
    }

    @PostMapping("/check")
    public Result<List<String>> check(@Valid @RequestBody SensitiveWordCheckRequest req) {
        return Result.ok(sensitiveWordService.match(req.getText()).stream()
                .map(m -> m.word())
                .toList());
    }
}
