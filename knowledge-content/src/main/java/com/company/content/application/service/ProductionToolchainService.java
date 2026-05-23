package com.company.content.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.common.exception.BizException;
import com.company.content.domain.model.*;
import com.company.content.infrastructure.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductionToolchainService {

    private final ContentVersionMapper versionMapper;
    private final AuditRecordMapper auditMapper;
    private final ContentTemplateMapper templateMapper;
    private final ScheduledPublishMapper scheduledMapper;
    private final ContentMapper contentMapper;

    public ProductionToolchainService(ContentVersionMapper versionMapper, AuditRecordMapper auditMapper,
                                       ContentTemplateMapper templateMapper, ScheduledPublishMapper scheduledMapper,
                                       ContentMapper contentMapper) {
        this.versionMapper = versionMapper;
        this.auditMapper = auditMapper;
        this.templateMapper = templateMapper;
        this.scheduledMapper = scheduledMapper;
        this.contentMapper = contentMapper;
    }

    // === Version History ===
    @Transactional
    public ContentVersion saveVersion(Long contentId, String title, String body, String changeSummary, Long userId) {
        long count = versionMapper.selectCount(
            new LambdaQueryWrapper<ContentVersion>().eq(ContentVersion::getContentId, contentId));
        ContentVersion v = new ContentVersion();
        v.setContentId(contentId);
        v.setVersionNumber((int) count + 1);
        v.setTitle(title);
        v.setBody(body);
        v.setChangeSummary(changeSummary);
        v.setCreatedBy(userId);
        versionMapper.insert(v);
        return v;
    }

    public List<ContentVersion> listVersions(Long contentId) {
        return versionMapper.selectList(
            new LambdaQueryWrapper<ContentVersion>()
                .eq(ContentVersion::getContentId, contentId)
                .orderByDesc(ContentVersion::getVersionNumber));
    }

    public ContentVersion getVersion(Long versionId) {
        ContentVersion v = versionMapper.selectById(versionId);
        if (v == null) throw BizException.notFound("版本");
        return v;
    }

    // === Audit Workflow ===
    @Transactional
    public AuditRecord submitAudit(String targetType, Long targetId, Long submitterId) {
        AuditRecord r = new AuditRecord();
        r.setTargetType(targetType);
        r.setTargetId(targetId);
        r.setSubmitterId(submitterId);
        r.setStatus("PENDING");
        r.setSubmittedAt(LocalDateTime.now());
        auditMapper.insert(r);
        return r;
    }

    public List<AuditRecord> listPendingAudits() {
        return auditMapper.selectList(
            new LambdaQueryWrapper<AuditRecord>().eq(AuditRecord::getStatus, "PENDING"));
    }

    @Transactional
    public void approveAudit(Long auditId, Long reviewerId) {
        AuditRecord r = auditMapper.selectById(auditId);
        if (r == null) throw BizException.notFound("审核记录");
        r.setStatus("APPROVED");
        r.setReviewerId(reviewerId);
        r.setReviewedAt(LocalDateTime.now());
        auditMapper.updateById(r);
    }

    @Transactional
    public void rejectAudit(Long auditId, Long reviewerId, String reason) {
        AuditRecord r = auditMapper.selectById(auditId);
        if (r == null) throw BizException.notFound("审核记录");
        r.setStatus("REJECTED");
        r.setReviewerId(reviewerId);
        r.setRejectReason(reason);
        r.setReviewedAt(LocalDateTime.now());
        auditMapper.updateById(r);
    }

    // === Content Templates ===
    public List<ContentTemplate> listTemplates() {
        return templateMapper.selectList(new LambdaQueryWrapper<>());
    }

    public ContentTemplate getTemplate(Long id) {
        ContentTemplate t = templateMapper.selectById(id);
        if (t == null) throw BizException.notFound("模板");
        return t;
    }

    @Transactional
    public ContentTemplate createTemplate(String name, String description, String contentType, String body, Long userId) {
        ContentTemplate t = new ContentTemplate();
        t.setName(name);
        t.setDescription(description);
        t.setContentType(contentType);
        t.setBody(body);
        t.setIsSystem(0);
        t.setCreatedBy(userId);
        templateMapper.insert(t);
        return t;
    }

    @Transactional
    public void deleteTemplate(Long id) {
        templateMapper.deleteById(id);
    }

    // === Scheduled Publish ===
    @Transactional
    public ScheduledPublish schedule(Long contentId, LocalDateTime scheduledAt) {
        ScheduledPublish s = new ScheduledPublish();
        s.setContentId(contentId);
        s.setScheduledAt(scheduledAt);
        s.setStatus("PENDING");
        scheduledMapper.insert(s);
        return s;
    }

    @Transactional
    public void cancelSchedule(Long id) {
        ScheduledPublish s = scheduledMapper.selectById(id);
        if (s == null) throw BizException.notFound("定时任务");
        s.setStatus("CANCELLED");
        scheduledMapper.updateById(s);
    }

    public List<ScheduledPublish> listPendingSchedules() {
        return scheduledMapper.selectList(
            new LambdaQueryWrapper<ScheduledPublish>().eq(ScheduledPublish::getStatus, "PENDING"));
    }
}
