package com.company.content.application.service;

import com.company.common.exception.BizException;
import com.company.content.domain.model.*;
import com.company.content.infrastructure.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionToolchainServiceTest {

    @Mock
    private ContentVersionMapper versionMapper;

    @Mock
    private AuditRecordMapper auditMapper;

    @Mock
    private ContentTemplateMapper templateMapper;

    @Mock
    private ScheduledPublishMapper scheduledMapper;

    @Mock
    private ContentMapper contentMapper;

    @InjectMocks
    private ProductionToolchainService service;

    @Test
    void shouldSaveFirstVersion() {
        when(versionMapper.selectCount(any())).thenReturn(0L);
        when(versionMapper.insert(any())).thenReturn(1);

        ContentVersion v = service.saveVersion(1L, "标题", "正文", "初始版本", 1L);

        assertThat(v.getVersionNumber()).isEqualTo(1);
        assertThat(v.getContentId()).isEqualTo(1L);
        assertThat(v.getTitle()).isEqualTo("标题");
        verify(versionMapper).insert(any());
    }

    @Test
    void shouldSaveSecondVersion() {
        when(versionMapper.selectCount(any())).thenReturn(1L);
        when(versionMapper.insert(any())).thenReturn(1);

        ContentVersion v = service.saveVersion(1L, "标题v2", "正文v2", "更新", 1L);

        assertThat(v.getVersionNumber()).isEqualTo(2);
    }

    @Test
    void shouldListVersions() {
        ContentVersion v1 = new ContentVersion();
        v1.setContentId(1L);
        v1.setVersionNumber(2);
        ContentVersion v2 = new ContentVersion();
        v2.setContentId(1L);
        v2.setVersionNumber(1);
        when(versionMapper.selectList(any())).thenReturn(List.of(v1, v2));

        List<ContentVersion> versions = service.listVersions(1L);

        assertThat(versions).hasSize(2);
    }

    @Test
    void shouldGetVersion() {
        ContentVersion v = new ContentVersion();
        v.setId(1L);
        v.setVersionNumber(1);
        when(versionMapper.selectById(1L)).thenReturn(v);

        ContentVersion result = service.getVersion(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenVersionNotFound() {
        when(versionMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> service.getVersion(999L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("版本");
    }

    @Test
    void shouldSubmitAudit() {
        when(auditMapper.insert(any())).thenReturn(1);

        AuditRecord r = service.submitAudit("CONTENT", 1L, 1L);

        assertThat(r.getTargetType()).isEqualTo("CONTENT");
        assertThat(r.getTargetId()).isEqualTo(1L);
        assertThat(r.getStatus()).isEqualTo("PENDING");
        assertThat(r.getSubmittedAt()).isNotNull();
    }

    @Test
    void shouldListPendingAudits() {
        AuditRecord r = new AuditRecord();
        r.setStatus("PENDING");
        when(auditMapper.selectList(any())).thenReturn(List.of(r));

        List<AuditRecord> result = service.listPendingAudits();
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldApproveAudit() {
        AuditRecord r = new AuditRecord();
        r.setId(1L);
        r.setStatus("PENDING");
        when(auditMapper.selectById(1L)).thenReturn(r);
        when(auditMapper.updateById(any())).thenReturn(1);

        service.approveAudit(1L, 2L);

        assertThat(r.getStatus()).isEqualTo("APPROVED");
        assertThat(r.getReviewerId()).isEqualTo(2L);
        assertThat(r.getReviewedAt()).isNotNull();
    }

    @Test
    void shouldThrowWhenApproveNonExistentAudit() {
        when(auditMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> service.approveAudit(999L, 2L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("审核记录");
    }

    @Test
    void shouldRejectAudit() {
        AuditRecord r = new AuditRecord();
        r.setId(1L);
        r.setStatus("PENDING");
        when(auditMapper.selectById(1L)).thenReturn(r);
        when(auditMapper.updateById(any())).thenReturn(1);

        service.rejectAudit(1L, 2L, "内容违规");

        assertThat(r.getStatus()).isEqualTo("REJECTED");
        assertThat(r.getRejectReason()).isEqualTo("内容违规");
        assertThat(r.getReviewerId()).isEqualTo(2L);
    }

    @Test
    void shouldThrowWhenRejectNonExistentAudit() {
        when(auditMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> service.rejectAudit(999L, 2L, "原因"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("审核记录");
    }

    @Test
    void shouldListTemplates() {
        ContentTemplate t = new ContentTemplate();
        t.setName("技术文档");
        when(templateMapper.selectList(any())).thenReturn(List.of(t));

        List<ContentTemplate> result = service.listTemplates();
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetTemplate() {
        ContentTemplate t = new ContentTemplate();
        t.setId(1L);
        t.setName("技术文档");
        when(templateMapper.selectById(1L)).thenReturn(t);

        ContentTemplate result = service.getTemplate(1L);
        assertThat(result.getName()).isEqualTo("技术文档");
    }

    @Test
    void shouldThrowWhenTemplateNotFound() {
        when(templateMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> service.getTemplate(999L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("模板");
    }

    @Test
    void shouldCreateTemplate() {
        when(templateMapper.insert(any())).thenReturn(1);

        ContentTemplate t = service.createTemplate("新模板", "描述", "MARKDOWN", "模板内容", 1L);

        assertThat(t.getName()).isEqualTo("新模板");
        assertThat(t.getContentType()).isEqualTo("MARKDOWN");
        assertThat(t.getIsSystem()).isEqualTo(0);
        assertThat(t.getCreatedBy()).isEqualTo(1L);
    }

    @Test
    void shouldDeleteTemplate() {
        when(templateMapper.deleteById(1L)).thenReturn(1);

        service.deleteTemplate(1L);
        verify(templateMapper).deleteById(1L);
    }

    @Test
    void shouldSchedulePublish() {
        when(scheduledMapper.insert(any())).thenReturn(1);

        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(1);
        ScheduledPublish s = service.schedule(1L, scheduledAt);

        assertThat(s.getContentId()).isEqualTo(1L);
        assertThat(s.getScheduledAt()).isEqualTo(scheduledAt);
        assertThat(s.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldCancelSchedule() {
        ScheduledPublish s = new ScheduledPublish();
        s.setId(1L);
        s.setStatus("PENDING");
        when(scheduledMapper.selectById(1L)).thenReturn(s);
        when(scheduledMapper.updateById(any())).thenReturn(1);

        service.cancelSchedule(1L);

        assertThat(s.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void shouldThrowWhenCancelNonExistentSchedule() {
        when(scheduledMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> service.cancelSchedule(999L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("定时任务");
    }

    @Test
    void shouldListPendingSchedules() {
        ScheduledPublish s = new ScheduledPublish();
        s.setStatus("PENDING");
        when(scheduledMapper.selectList(any())).thenReturn(List.of(s));

        List<ScheduledPublish> result = service.listPendingSchedules();
        assertThat(result).hasSize(1);
    }
}
