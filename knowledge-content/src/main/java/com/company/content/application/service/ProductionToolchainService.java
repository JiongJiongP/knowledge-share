package com.company.content.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.common.exception.BizException;
import com.company.content.application.dto.AuditRecordVO;
import com.company.content.domain.event.ContentSubmittedForAuditEvent;
import com.company.content.domain.model.*;
import com.company.content.infrastructure.mapper.*;
import com.company.userauth.domain.model.User;
import com.company.userauth.infrastructure.mapper.UserMapper;
import com.company.userauth.infrastructure.util.UserDisplayUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductionToolchainService {

    private static final Map<String, String> TARGET_TYPE_NAMES = Map.of(
            "CONTENT", "内容", "COMMENT", "评论");
    private static final Map<String, String> STATUS_NAMES = Map.of(
            "PENDING", "待审核", "APPROVED", "已通过", "REJECTED", "已驳回");

    private final ContentVersionMapper versionMapper;
    private final AuditRecordMapper auditMapper;
    private final ContentTemplateMapper templateMapper;
    private final ScheduledPublishMapper scheduledMapper;
    private final ContentMapper contentMapper;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    public ProductionToolchainService(ContentVersionMapper versionMapper, AuditRecordMapper auditMapper,
                                       ContentTemplateMapper templateMapper, ScheduledPublishMapper scheduledMapper,
                                       ContentMapper contentMapper, UserMapper userMapper,
                                       ApplicationEventPublisher eventPublisher) {
        this.versionMapper = versionMapper;
        this.auditMapper = auditMapper;
        this.templateMapper = templateMapper;
        this.scheduledMapper = scheduledMapper;
        this.contentMapper = contentMapper;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
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
    public AuditRecord submitAudit(String targetType, Long targetId, Long submitterId, String submitterName) {
        AuditRecord r = new AuditRecord();
        r.setTargetType(targetType);
        r.setTargetId(targetId);
        r.setSubmitterId(submitterId);
        r.setStatus("PENDING");
        r.setSubmittedAt(LocalDateTime.now());
        auditMapper.insert(r);

        String title = resolveTitle(targetType, targetId);
        eventPublisher.publishEvent(
                new ContentSubmittedForAuditEvent(targetId, title, submitterId, submitterName, targetType, r.getId()));
        return r;
    }

    private String resolveTitle(String targetType, Long targetId) {
        if ("CONTENT".equals(targetType)) {
            KnowledgeContent c = contentMapper.selectById(targetId);
            return c != null ? c.getTitle() : "未知内容";
        }
        return "未知";
    }

    public List<AuditRecordVO> listPendingAudits() {
        List<AuditRecord> records = auditMapper.selectList(
                new LambdaQueryWrapper<AuditRecord>().eq(AuditRecord::getStatus, "PENDING"));
        if (records.isEmpty()) return Collections.emptyList();

        Set<Long> contentIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();
        for (AuditRecord r : records) {
            if ("CONTENT".equals(r.getTargetType())) contentIds.add(r.getTargetId());
            if (r.getSubmitterId() != null) userIds.add(r.getSubmitterId());
            if (r.getReviewerId() != null) userIds.add(r.getReviewerId());
        }

        Map<Long, String> titleMap = Collections.emptyMap();
        if (!contentIds.isEmpty()) {
            titleMap = contentMapper.selectList(
                    new LambdaQueryWrapper<KnowledgeContent>().in(KnowledgeContent::getId, contentIds))
                    .stream().collect(Collectors.toMap(KnowledgeContent::getId, KnowledgeContent::getTitle, (a, b) -> a));
        }
        Map<Long, String> nameMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            nameMap = userMapper.selectList(
                    new LambdaQueryWrapper<User>().in(User::getId, userIds))
                    .stream().collect(Collectors.toMap(User::getId,
                            UserDisplayUtil::resolve, (a, b) -> a));
        }

        List<AuditRecordVO> vos = new ArrayList<>();
        for (AuditRecord r : records) {
            AuditRecordVO vo = new AuditRecordVO();
            vo.setId(r.getId());
            vo.setTargetType(r.getTargetType());
            vo.setTargetTypeName(TARGET_TYPE_NAMES.getOrDefault(r.getTargetType(), r.getTargetType()));
            vo.setTargetId(r.getTargetId());
            vo.setTargetTitle(titleMap.getOrDefault(r.getTargetId(), String.valueOf(r.getTargetId())));
            vo.setSubmitterId(r.getSubmitterId());
            vo.setSubmitterName(nameMap.getOrDefault(r.getSubmitterId(), String.valueOf(r.getSubmitterId())));
            vo.setReviewerId(r.getReviewerId());
            vo.setReviewerName(r.getReviewerId() != null ? nameMap.getOrDefault(r.getReviewerId(), String.valueOf(r.getReviewerId())) : null);
            vo.setStatus(r.getStatus());
            vo.setStatusName(STATUS_NAMES.getOrDefault(r.getStatus(), r.getStatus()));
            vo.setRejectReason(r.getRejectReason());
            vo.setSubmittedAt(r.getSubmittedAt());
            vo.setReviewedAt(r.getReviewedAt());
            vos.add(vo);
        }
        return vos;
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
