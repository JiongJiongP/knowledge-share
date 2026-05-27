package com.company.web.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.enums.PublishStatus;
import com.company.content.infrastructure.mapper.AuditRecordMapper;
import com.company.content.infrastructure.mapper.ContentMapper;
import com.company.content.domain.model.AuditRecord;
import com.company.social.domain.model.Group;
import com.company.social.domain.model.GroupMember;
import com.company.social.infrastructure.mapper.GroupMapper;
import com.company.social.infrastructure.mapper.GroupMemberMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TodoService {

    private final ContentMapper contentMapper;
    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final AuditRecordMapper auditRecordMapper;

    public TodoService(ContentMapper contentMapper, GroupMapper groupMapper,
                       GroupMemberMapper groupMemberMapper, AuditRecordMapper auditRecordMapper) {
        this.contentMapper = contentMapper;
        this.groupMapper = groupMapper;
        this.groupMemberMapper = groupMemberMapper;
        this.auditRecordMapper = auditRecordMapper;
    }

    public Map<String, Object> getCounts(Long userId, boolean isAdmin) {
        // 1. My drafts
        long draftCount = contentMapper.selectCount(
                new LambdaQueryWrapper<KnowledgeContent>()
                        .eq(KnowledgeContent::getCreatedBy, userId)
                        .eq(KnowledgeContent::getStatus, PublishStatus.DRAFT)
        );

        // 2. Pending group join approvals (groups I own)
        long pendingApprovalCount = 0;
        var myGroups = groupMapper.selectList(
                new LambdaQueryWrapper<Group>()
                        .eq(Group::getOwnerId, userId)
        );
        if (!myGroups.isEmpty()) {
            var groupIds = myGroups.stream().map(Group::getId).toList();
            pendingApprovalCount = groupMemberMapper.selectCount(
                    new LambdaQueryWrapper<GroupMember>()
                            .in(GroupMember::getGroupId, groupIds)
                            .eq(GroupMember::getStatus, "PENDING")
            );
        }

        // 3. Pending audit (admin only)
        long pendingAuditCount = 0;
        if (isAdmin) {
            pendingAuditCount = auditRecordMapper.selectCount(
                    new LambdaQueryWrapper<AuditRecord>()
                            .eq(AuditRecord::getStatus, "PENDING")
            );
        }

        return Map.of(
                "draftCount", draftCount,
                "pendingApprovalCount", pendingApprovalCount,
                "pendingAuditCount", pendingAuditCount
        );
    }
}
