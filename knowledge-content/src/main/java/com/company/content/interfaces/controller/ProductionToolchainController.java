package com.company.content.interfaces.controller;

import com.company.common.result.Result;
import com.company.content.application.dto.*;
import com.company.content.application.service.ProductionToolchainService;
import com.company.content.domain.model.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ProductionToolchainController {

    private final ProductionToolchainService toolchainService;

    public ProductionToolchainController(ProductionToolchainService toolchainService) {
        this.toolchainService = toolchainService;
    }

    // === Version History ===
    @GetMapping("/api/contents/{id}/versions")
    public Result<List<ContentVersion>> listVersions(@PathVariable Long id) {
        return Result.ok(toolchainService.listVersions(id));
    }

    @GetMapping("/api/contents/{id}/versions/{versionId}")
    public Result<ContentVersion> getVersion(@PathVariable Long id, @PathVariable Long versionId) {
        return Result.ok(toolchainService.getVersion(versionId));
    }

    @PostMapping("/api/contents/{id}/versions")
    public Result<ContentVersion> saveVersion(@PathVariable Long id, @Valid @RequestBody SaveVersionRequest req, Authentication auth) {
        ContentVersion v = toolchainService.saveVersion(id, req.getTitle(), req.getBody(),
                req.getChangeSummary(), (Long) auth.getPrincipal());
        return Result.ok(v);
    }

    // === Audit ===
    @GetMapping("/api/admin/audit/pending")
    public Result<List<AuditRecord>> listPendingAudits() {
        return Result.ok(toolchainService.listPendingAudits());
    }

    @PostMapping("/api/admin/audit/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, Authentication auth) {
        toolchainService.approveAudit(id, (Long) auth.getPrincipal());
        return Result.ok(null);
    }

    @PostMapping("/api/admin/audit/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @Valid @RequestBody RejectAuditRequest req, Authentication auth) {
        toolchainService.rejectAudit(id, (Long) auth.getPrincipal(), req.getReason());
        return Result.ok(null);
    }

    // === Templates ===
    @GetMapping("/api/templates")
    public Result<List<ContentTemplate>> listTemplates() {
        return Result.ok(toolchainService.listTemplates());
    }

    @GetMapping("/api/templates/{id}")
    public Result<ContentTemplate> getTemplate(@PathVariable Long id) {
        return Result.ok(toolchainService.getTemplate(id));
    }

    @PostMapping("/api/templates")
    public Result<ContentTemplate> createTemplate(@Valid @RequestBody CreateTemplateRequest req, Authentication auth) {
        String contentType = req.getContentType() != null ? req.getContentType() : "MARKDOWN";
        ContentTemplate t = toolchainService.createTemplate(req.getName(), req.getDescription(),
                contentType, req.getBody(), (Long) auth.getPrincipal());
        return Result.ok(t);
    }

    @DeleteMapping("/api/templates/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        toolchainService.deleteTemplate(id);
        return Result.ok(null);
    }

    // === Scheduled Publish ===
    @PostMapping("/api/contents/{id}/schedule")
    public Result<ScheduledPublish> schedule(@PathVariable Long id, @Valid @RequestBody SchedulePublishRequest req) {
        LocalDateTime at = LocalDateTime.parse(req.getScheduledAt());
        return Result.ok(toolchainService.schedule(id, at));
    }

    @DeleteMapping("/api/contents/{id}/schedule")
    public Result<Void> cancelSchedule(@PathVariable Long id, @Valid @RequestBody CancelScheduleRequest req) {
        toolchainService.cancelSchedule(req.getScheduleId());
        return Result.ok(null);
    }
}
