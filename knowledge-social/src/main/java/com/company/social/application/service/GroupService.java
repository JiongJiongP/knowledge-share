package com.company.social.application.service;

import com.company.common.exception.BizException;
import com.company.common.result.PageResult;
import com.company.social.domain.model.Group;
import com.company.social.domain.model.GroupMember;
import com.company.social.domain.repository.GroupRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public PageResult<Group> listPublic(int page, int size) {
        return PageResult.of(
                groupRepository.findPublic(page, size),
                groupRepository.countPublic(),
                page, size
        );
    }

    public Group getById(Long id) {
        Group g = groupRepository.findById(id);
        if (g == null) {
            throw BizException.notFound("群组");
        }
        return g;
    }

    @Transactional
    public Group create(Long userId, String name, String description) {
        Group g = new Group();
        g.setName(name);
        g.setDescription(description);
        g.setOwnerId(userId);
        g.setVisibility("PUBLIC");
        g.setStatus("APPROVED");
        groupRepository.insert(g);

        GroupMember gm = new GroupMember();
        gm.setGroupId(g.getId());
        gm.setUserId(userId);
        gm.setRole("OWNER");
        gm.setStatus("APPROVED");
        gm.setJoinedAt(LocalDateTime.now());
        groupRepository.insertMember(gm);

        return g;
    }

    @Transactional
    public void requestJoin(Long groupId, Long userId) {
        getById(groupId);
        GroupMember existing = groupRepository.findMember(groupId, userId);
        if (existing != null) {
            throw BizException.badRequest("已是群组成员或已提交申请");
        }
        GroupMember gm = new GroupMember();
        gm.setGroupId(groupId);
        gm.setUserId(userId);
        gm.setRole("MEMBER");
        gm.setStatus("PENDING");
        try {
            groupRepository.insertMember(gm);
        } catch (DataIntegrityViolationException e) {
            throw BizException.badRequest("已是群组成员或已提交申请");
        }
    }

    @Transactional
    public void approveMember(Long groupId, Long userId, Long ownerId) {
        Group g = getById(groupId);
        if (!g.getOwnerId().equals(ownerId)) {
            throw BizException.forbidden();
        }
        GroupMember member = groupRepository.findMember(groupId, userId);
        if (member == null) {
            throw BizException.notFound("申请");
        }
        boolean updated = groupRepository.updateMemberStatus(member.getId(), "APPROVED");
        if (!updated) {
            throw BizException.badRequest("该申请已处理");
        }
    }

    @Transactional
    public void rejectMember(Long groupId, Long userId, Long ownerId) {
        Group g = getById(groupId);
        if (!g.getOwnerId().equals(ownerId)) {
            throw BizException.forbidden();
        }
        GroupMember member = groupRepository.findMember(groupId, userId);
        if (member == null) {
            throw BizException.notFound("申请");
        }
        boolean updated = groupRepository.updateMemberStatus(member.getId(), "REJECTED");
        if (!updated) {
            throw BizException.badRequest("该申请已处理");
        }
    }

    @Transactional
    public void removeMember(Long groupId, Long userId, Long ownerId) {
        Group g = getById(groupId);
        if (!g.getOwnerId().equals(ownerId)) {
            throw BizException.forbidden();
        }
        if (userId.equals(g.getOwnerId())) {
            throw BizException.badRequest("不能移除群主");
        }
        groupRepository.deleteMember(groupId, userId);
    }

    public List<GroupMember> listMembers(Long groupId) {
        return groupRepository.findMembers(groupId);
    }

    public List<GroupMember> listPendingMembers(Long groupId, Long ownerId) {
        Group g = getById(groupId);
        if (!g.getOwnerId().equals(ownerId)) {
            throw BizException.forbidden();
        }
        return groupRepository.findPendingMembers(groupId);
    }
}
