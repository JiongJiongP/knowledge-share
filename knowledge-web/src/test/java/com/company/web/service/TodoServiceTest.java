package com.company.web.service;

import com.company.content.domain.model.AuditRecord;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.enums.PublishStatus;
import com.company.content.infrastructure.mapper.AuditRecordMapper;
import com.company.content.infrastructure.mapper.ContentMapper;
import com.company.social.domain.model.Group;
import com.company.social.domain.model.GroupMember;
import com.company.social.infrastructure.mapper.GroupMapper;
import com.company.social.infrastructure.mapper.GroupMemberMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private ContentMapper contentMapper;

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private GroupMemberMapper groupMemberMapper;

    @Mock
    private AuditRecordMapper auditRecordMapper;

    @InjectMocks
    private TodoService todoService;

    @Test
    void shouldReturnCountsForRegularUser() {
        when(contentMapper.selectCount(any())).thenReturn(3L);
        when(groupMapper.selectList(any())).thenReturn(List.of());

        Map<String, Object> counts = todoService.getCounts(1L, false);

        assertThat(counts).containsEntry("draftCount", 3L);
        assertThat(counts).containsEntry("pendingApprovalCount", 0L);
        assertThat(counts).containsEntry("pendingAuditCount", 0L);
    }

    @Test
    void shouldReturnCountsForAdmin() {
        when(contentMapper.selectCount(any())).thenReturn(5L);
        when(groupMapper.selectList(any())).thenReturn(List.of());
        when(auditRecordMapper.selectCount(any())).thenReturn(2L);

        Map<String, Object> counts = todoService.getCounts(1L, true);

        assertThat(counts).containsEntry("draftCount", 5L);
        assertThat(counts).containsEntry("pendingAuditCount", 2L);
    }

    @Test
    void shouldCountPendingGroupApprovals() {
        Group g = new Group();
        g.setId(1L);
        g.setOwnerId(1L);

        when(contentMapper.selectCount(any())).thenReturn(0L);
        when(groupMapper.selectList(any())).thenReturn(List.of(g));
        when(groupMemberMapper.selectCount(any())).thenReturn(2L);

        Map<String, Object> counts = todoService.getCounts(1L, false);

        assertThat(counts).containsEntry("pendingApprovalCount", 2L);
        assertThat(counts).containsEntry("firstPendingGroupId", 1L);
    }

    @Test
    void shouldReturnZeroFirstPendingGroupIdWhenNoPending() {
        when(contentMapper.selectCount(any())).thenReturn(0L);
        when(groupMapper.selectList(any())).thenReturn(List.of());

        Map<String, Object> counts = todoService.getCounts(1L, false);

        assertThat(counts).containsEntry("firstPendingGroupId", 0L);
    }
}
